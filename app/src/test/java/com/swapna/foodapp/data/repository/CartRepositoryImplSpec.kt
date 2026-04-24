package com.swapna.foodapp.data.repository

import app.cash.turbine.test
import com.swapna.foodapp.data.local.dao.CartDao
import com.swapna.foodapp.data.mapper.EntityMapper
import com.swapna.foodapp.utils.AppBusinessRules
import com.swapna.foodapp.utils.fakeCartItem
import com.swapna.foodapp.utils.fakeCartItemEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class CartRepositoryImplSpec : FunSpec({

    val cartDao      = mockk<CartDao>()
    val entityMapper = mockk<EntityMapper>()

    fun createRepo() = CartRepositoryImpl(
        cartDao       = cartDao,
        entityMapper  = entityMapper,
        ioDispatcher  = UnconfinedTestDispatcher(),
    )

    beforeEach {
        clearAllMocks()
        Dispatchers.setMain(UnconfinedTestDispatcher())

        // Default stubs
        every { cartDao.getAllItems() }   returns flowOf(emptyList())
        every { cartDao.getItemCount() }  returns flowOf(0)
        coEvery { cartDao.insert(any()) } just runs
        coEvery { cartDao.updateQuantity(any(), any()) } just runs
        coEvery { cartDao.deleteById(any()) } just runs
        coEvery { cartDao.clearAll() } just runs
        coEvery { cartDao.getByMenuItemId(any()) } returns null

        coEvery { cartDao.getById(any()) }           returns null        // ← ADD
        coEvery { cartDao.getQuantityById(any()) }   returns null
    }

    afterEach { Dispatchers.resetMain() }

    // ── getCartItems ──────────────────────────────────────────

    test("getCartItems: emits empty list when cart is empty") {
        every { cartDao.getAllItems() } returns flowOf(emptyList())

        val result = createRepo().getCartItems().test {
            awaitItem().shouldBeEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCartItems: maps entities to domain CartItems") {
        val entity   = fakeCartItemEntity("c1", "m1", qty = 2)
        val cartItem = fakeCartItem(id = "c1", qty = 2)

        every { cartDao.getAllItems() } returns flowOf(listOf(entity))
        every { entityMapper.cartItemToDomain(entity) } returns cartItem

        createRepo().getCartItems().test {
            val items = awaitItem()
            items shouldHaveSize 1
            items.first().id shouldBe "c1"
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCartItems: maps multiple entities correctly") {
        val entity1   = fakeCartItemEntity("c1", "m1", qty = 1)
        val entity2   = fakeCartItemEntity("c2", "m2", qty = 2)
        val cartItem1 = fakeCartItem(id = "c1", qty = 1)
        val cartItem2 = fakeCartItem(id = "c2", qty = 2)

        every { cartDao.getAllItems() } returns flowOf(listOf(entity1, entity2))
        every { entityMapper.cartItemToDomain(entity1) } returns cartItem1
        every { entityMapper.cartItemToDomain(entity2) } returns cartItem2

        createRepo().getCartItems().test {
            awaitItem() shouldHaveSize 2
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── getCartItemCount ──────────────────────────────────────

    test("getCartItemCount: emits 0 when cart is empty") {
        every { cartDao.getItemCount() } returns flowOf(0)

        createRepo().getCartItemCount().test {
            awaitItem() shouldBe 0
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCartItemCount: emits correct count from dao") {
        every { cartDao.getItemCount() } returns flowOf(3)

        createRepo().getCartItemCount().test {
            awaitItem() shouldBe 3
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── getCartTotal ──────────────────────────────────────────

    test("getCartTotal: returns zero breakdown when cart is empty") {
        every { cartDao.getAllItems() } returns flowOf(emptyList())

        createRepo().getCartTotal().test {
            val breakdown = awaitItem()
            breakdown.subtotal    shouldBe 0.0
            breakdown.deliveryFee shouldBe 0.0
            breakdown.taxes       shouldBe 0.0
            breakdown.total       shouldBe 0.0
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCartTotal: calculates subtotal from item totalPrice") {
        val entity   = fakeCartItemEntity("c1", "m1", qty = 1)
        val cartItem = fakeCartItem(id = "c1", price = 249.0, qty = 1)

        every { cartDao.getAllItems() } returns flowOf(listOf(entity))
        every { entityMapper.cartItemToDomain(entity) } returns cartItem

        createRepo().getCartTotal().test {
            val breakdown = awaitItem()
            breakdown.subtotal shouldBe 249.0
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCartTotal: adds delivery fee when subtotal below threshold") {
        val entity   = fakeCartItemEntity("c1", "m1", qty = 1)
        val cartItem = fakeCartItem(id = "c1", price = 50.0, qty = 1)

        every { cartDao.getAllItems() } returns flowOf(listOf(entity))
        every { entityMapper.cartItemToDomain(entity) } returns cartItem

        createRepo().getCartTotal().test {
            val breakdown = awaitItem()
            breakdown.deliveryFee shouldBe AppBusinessRules.DEFAULT_DELIVERY_FEE
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCartTotal: no delivery fee when subtotal above threshold") {
        val price    = AppBusinessRules.FREE_DELIVERY_ABOVE + 100.0
        val entity   = fakeCartItemEntity("c1", "m1", qty = 1)
        val cartItem = fakeCartItem(id = "c1", price = price, qty = 1)

        every { cartDao.getAllItems() } returns flowOf(listOf(entity))
        every { entityMapper.cartItemToDomain(entity) } returns cartItem

        createRepo().getCartTotal().test {
            val breakdown = awaitItem()
            breakdown.deliveryFee shouldBe 0.0
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCartTotal: calculates taxes as GST_RATE × subtotal") {
        val entity   = fakeCartItemEntity("c1", "m1", qty = 1)
        val cartItem = fakeCartItem(id = "c1", price = 249.0, qty = 1)

        every { cartDao.getAllItems() } returns flowOf(listOf(entity))
        every { entityMapper.cartItemToDomain(entity) } returns cartItem

        createRepo().getCartTotal().test {
            val breakdown     = awaitItem()
            val expectedTaxes = 249.0 * AppBusinessRules.GST_RATE
            breakdown.taxes shouldBe expectedTaxes
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCartTotal: total equals subtotal + deliveryFee + taxes") {
        val entity   = fakeCartItemEntity("c1", "m1", qty = 1)
        val cartItem = fakeCartItem(id = "c1", price = 249.0, qty = 1)

        every { cartDao.getAllItems() } returns flowOf(listOf(entity))
        every { entityMapper.cartItemToDomain(entity) } returns cartItem

        createRepo().getCartTotal().test {
            val breakdown = awaitItem()
            val expected  = breakdown.subtotal +
                    breakdown.deliveryFee +
                    breakdown.taxes
            breakdown.total shouldBe expected
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── addItem ───────────────────────────────────────────────

    test("addItem: calls cartDao.insert with mapped entity") {
        val cartItem = fakeCartItem(id = "c1", price = 249.0, qty = 1)
        val entity   = fakeCartItemEntity("c1", "m1", qty = 1)

        every { entityMapper.cartItemToEntity(cartItem) } returns entity
        coEvery { cartDao.insert(entity) } just runs

        createRepo().addItem(cartItem)

        coVerify { cartDao.insert(entity) }
    }

    test("addItem: maps CartItem to entity before inserting") {
        val cartItem = fakeCartItem(id = "c1", price = 249.0, qty = 2)
        val entity   = fakeCartItemEntity("c1", "m1", qty = 2)

        every { entityMapper.cartItemToEntity(cartItem) } returns entity
        coEvery { cartDao.insert(entity) } just runs

        createRepo().addItem(cartItem)

        coVerify { entityMapper.cartItemToEntity(cartItem) }
    }

    // ── updateQuantity ────────────────────────────────────────

    test("updateQuantity: calls cartDao.updateQuantity with correct params") {
        createRepo().updateQuantity("c1", 3)

        coVerify { cartDao.updateQuantity("c1", 3) }
    }

    test("updateQuantity: passes itemId and quantity correctly") {
        createRepo().updateQuantity("m1", 5)

        coVerify { cartDao.updateQuantity("m1", 5) }
    }

    // ── removeItem ────────────────────────────────────────────

    test("removeItem: calls cartDao.deleteById with correct itemId") {
        createRepo().removeItem("c1")

        coVerify { cartDao.deleteById("c1") }
    }

    test("removeItem: passes correct id — different item") {
        createRepo().removeItem("c2")

        coVerify { cartDao.deleteById("c2") }
    }

    // ── clearCart ─────────────────────────────────────────────

    test("clearCart: calls cartDao.clearAll") {
        createRepo().clearCart()

        coVerify { cartDao.clearAll() }
    }

    // ── itemExists ────────────────────────────────────────────

    test("itemExists: returns false when item not in cart") {
        coEvery { cartDao.getByMenuItemId("m1") } returns null

        val result = createRepo().itemExists("m1")

        result shouldBe false
    }

    test("itemExists: returns true when item exists in cart") {
        val entity = fakeCartItemEntity("c1", "m1", qty = 1)
        coEvery { cartDao.getByMenuItemId("m1") } returns entity

        val result = createRepo().itemExists("m1")

        result shouldBe true
    }

    test("itemExists: checks correct menuItemId") {
        coEvery { cartDao.getByMenuItemId("m_unknown") } returns null

        val result = createRepo().itemExists("m_unknown")

        result shouldBe false
        coVerify { cartDao.getByMenuItemId("m_unknown") }
    }

    // ── getCartTotal via getAllItems flow ─────────────────────────
// WHY test getAllItems not getById?
// CartRepositoryImpl.getCartTotal() uses getAllItems()
// getById() in CartDao is used separately

    test("getCartTotal: delivery fee is 0 when subtotal exactly equals threshold") {
        val price    = AppBusinessRules.FREE_DELIVERY_ABOVE  // exactly at boundary
        val entity   = fakeCartItemEntity("c1", "m1", qty = 1)
        val cartItem = fakeCartItem(id = "c1", price = price, qty = 1)

        every { cartDao.getAllItems() } returns flowOf(listOf(entity))
        every { entityMapper.cartItemToDomain(entity) } returns cartItem

        createRepo().getCartTotal().test {
            val breakdown = awaitItem()
            // WHY 0.0 not DEFAULT_DELIVERY_FEE?
            // impl: subtotal >= FREE_DELIVERY_ABOVE → 0.0
            // exactly at threshold = free delivery
            breakdown.deliveryFee shouldBe 0.0
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCartTotal: multiple items subtotal summed correctly") {
        val entity1   = fakeCartItemEntity("c1", "m1", qty = 1)
        val entity2   = fakeCartItemEntity("c2", "m2", qty = 2)
        val cartItem1 = fakeCartItem(id = "c1", price = 249.0, qty = 1)
        val cartItem2 = fakeCartItem(id = "c2", price = 100.0, qty = 2)

        every { cartDao.getAllItems() } returns flowOf(listOf(entity1, entity2))
        every { entityMapper.cartItemToDomain(entity1) } returns cartItem1
        every { entityMapper.cartItemToDomain(entity2) } returns cartItem2

        createRepo().getCartTotal().test {
            val breakdown = awaitItem()
            // cartItem1.totalPrice = 249 × 1 = 249
            // cartItem2.totalPrice = 100 × 2 = 200
            // subtotal = 449
            breakdown.subtotal shouldBe 449.0
            cancelAndIgnoreRemainingEvents()
        }
    }

// ── itemExists via getByMenuItemId ────────────────────────────

    test("itemExists: uses getByMenuItemId not getById") {
        // WHY test this specifically?
        // itemExists() must check by MENU item id not cart item id
        // Using wrong DAO method = always returns false = bug
        coEvery { cartDao.getByMenuItemId("m1") } returns
                fakeCartItemEntity("c1", "m1", qty = 1)

        val result = createRepo().itemExists("m1")

        result shouldBe true
        coVerify { cartDao.getByMenuItemId("m1") }
        // getById must NOT be called — wrong method
        coVerify(exactly = 0) { cartDao.getById(any()) }
    }
})