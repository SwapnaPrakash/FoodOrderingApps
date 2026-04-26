package com.swapna.foodapp.data.repository

import app.cash.turbine.test
import com.swapna.foodapp.data.local.dao.CartDao
import com.swapna.foodapp.data.mapper.EntityMapper
import com.swapna.foodapp.utils.AppBusinessRules
import com.swapna.foodapp.utils.TestConstants.CART_ID_1
import com.swapna.foodapp.utils.TestConstants.CART_ID_2
import com.swapna.foodapp.utils.TestConstants.CART_QTY_1
import com.swapna.foodapp.utils.TestConstants.CART_QTY_2
import com.swapna.foodapp.utils.TestConstants.CART_QTY_3
import com.swapna.foodapp.utils.TestConstants.CART_QTY_5
import com.swapna.foodapp.utils.TestConstants.MENU_ID_1
import com.swapna.foodapp.utils.TestConstants.MENU_ID_2
import com.swapna.foodapp.utils.TestConstants.PRICE_100
import com.swapna.foodapp.utils.TestConstants.PRICE_249
import com.swapna.foodapp.utils.TestConstants.PRICE_50
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

    val cartDao = mockk<CartDao>()
    val entityMapper = mockk<EntityMapper>()

    fun createRepo() = CartRepositoryImpl(
        cartDao = cartDao,
        entityMapper = entityMapper,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    beforeEach {
        clearAllMocks()
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { cartDao.getAllItems() } returns flowOf(emptyList())
        every { cartDao.getItemCount() } returns flowOf(0)
        coEvery { cartDao.insert(any()) } just runs
        coEvery { cartDao.updateQuantity(any(), any()) } just runs
        coEvery { cartDao.deleteById(any()) } just runs
        coEvery { cartDao.clearAll() } just runs
        coEvery { cartDao.getByMenuItemId(any()) } returns null
        coEvery { cartDao.getById(any()) } returns null
        coEvery { cartDao.getQuantityById(any()) } returns null
    }

    afterEach { Dispatchers.resetMain() }

    // ── getCartItems ──────────────────────────────────────────

    test("getCartItems: emits empty list when cart is empty") {
        every { cartDao.getAllItems() } returns flowOf(emptyList())

        createRepo().getCartItems().test {
            awaitItem().shouldBeEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCartItems: maps entities to domain CartItems") {
        val entity = fakeCartItemEntity(CART_ID_1, MENU_ID_1, qty = CART_QTY_2)
        val cartItem = fakeCartItem(id = CART_ID_1, qty = CART_QTY_2)

        every { cartDao.getAllItems() } returns flowOf(listOf(entity))
        every { entityMapper.cartItemToDomain(entity) } returns cartItem

        createRepo().getCartItems().test {
            val items = awaitItem()
            items shouldHaveSize 1
            items.first().id shouldBe CART_ID_1
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCartItems: maps multiple entities correctly") {
        val entity1 = fakeCartItemEntity(CART_ID_1, MENU_ID_1, qty = CART_QTY_1)
        val entity2 = fakeCartItemEntity(CART_ID_2, MENU_ID_2, qty = CART_QTY_2)
        val cartItem1 = fakeCartItem(id = CART_ID_1, qty = CART_QTY_1)
        val cartItem2 = fakeCartItem(id = CART_ID_2, qty = CART_QTY_2)

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
        every { cartDao.getItemCount() } returns flowOf(CART_QTY_3)

        createRepo().getCartItemCount().test {
            awaitItem() shouldBe CART_QTY_3
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── getCartTotal ──────────────────────────────────────────

    test("getCartTotal: returns zero breakdown when cart is empty") {
        every { cartDao.getAllItems() } returns flowOf(emptyList())

        createRepo().getCartTotal().test {
            val breakdown = awaitItem()
            breakdown.subtotal shouldBe 0.0
            breakdown.deliveryFee shouldBe 0.0
            breakdown.taxes shouldBe 0.0
            breakdown.total shouldBe 0.0
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCartTotal: calculates subtotal from item totalPrice") {
        val entity = fakeCartItemEntity(CART_ID_1, MENU_ID_1, qty = CART_QTY_1)
        val cartItem = fakeCartItem(id = CART_ID_1, price = PRICE_249, qty = CART_QTY_1)

        every { cartDao.getAllItems() } returns flowOf(listOf(entity))
        every { entityMapper.cartItemToDomain(entity) } returns cartItem

        createRepo().getCartTotal().test {
            val breakdown = awaitItem()
            breakdown.subtotal shouldBe PRICE_249
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCartTotal: adds delivery fee when subtotal below threshold") {
        val entity = fakeCartItemEntity(CART_ID_1, MENU_ID_1, qty = CART_QTY_1)
        val cartItem = fakeCartItem(id = CART_ID_1, price = PRICE_50, qty = CART_QTY_1)

        every { cartDao.getAllItems() } returns flowOf(listOf(entity))
        every { entityMapper.cartItemToDomain(entity) } returns cartItem

        createRepo().getCartTotal().test {
            val breakdown = awaitItem()
            breakdown.deliveryFee shouldBe AppBusinessRules.DEFAULT_DELIVERY_FEE
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCartTotal: no delivery fee when subtotal above threshold") {
        val price = AppBusinessRules.FREE_DELIVERY_ABOVE + PRICE_100
        val entity = fakeCartItemEntity(CART_ID_1, MENU_ID_1, qty = CART_QTY_1)
        val cartItem = fakeCartItem(id = CART_ID_1, price = price, qty = CART_QTY_1)

        every { cartDao.getAllItems() } returns flowOf(listOf(entity))
        every { entityMapper.cartItemToDomain(entity) } returns cartItem

        createRepo().getCartTotal().test {
            val breakdown = awaitItem()
            breakdown.deliveryFee shouldBe 0.0
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCartTotal: calculates taxes as GST_RATE × subtotal") {
        val entity = fakeCartItemEntity(CART_ID_1, MENU_ID_1, qty = CART_QTY_1)
        val cartItem = fakeCartItem(id = CART_ID_1, price = PRICE_249, qty = CART_QTY_1)

        every { cartDao.getAllItems() } returns flowOf(listOf(entity))
        every { entityMapper.cartItemToDomain(entity) } returns cartItem

        createRepo().getCartTotal().test {
            val breakdown = awaitItem()
            val expectedTaxes = PRICE_249 * AppBusinessRules.GST_RATE
            breakdown.taxes shouldBe expectedTaxes
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCartTotal: total equals subtotal + deliveryFee + taxes") {
        val entity = fakeCartItemEntity(CART_ID_1, MENU_ID_1, qty = CART_QTY_1)
        val cartItem = fakeCartItem(id = CART_ID_1, price = PRICE_249, qty = CART_QTY_1)

        every { cartDao.getAllItems() } returns flowOf(listOf(entity))
        every { entityMapper.cartItemToDomain(entity) } returns cartItem

        createRepo().getCartTotal().test {
            val breakdown = awaitItem()
            val expected = breakdown.subtotal + breakdown.deliveryFee + breakdown.taxes
            breakdown.total shouldBe expected
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── addItem ───────────────────────────────────────────────

    test("addItem: calls cartDao.insert with mapped entity") {
        val cartItem = fakeCartItem(id = CART_ID_1, price = PRICE_249, qty = CART_QTY_1)
        val entity = fakeCartItemEntity(CART_ID_1, MENU_ID_1, qty = CART_QTY_1)

        every { entityMapper.cartItemToEntity(cartItem) } returns entity
        coEvery { cartDao.insert(entity) } just runs

        createRepo().addItem(cartItem)

        coVerify { cartDao.insert(entity) }
    }

    test("addItem: maps CartItem to entity before inserting") {
        val cartItem = fakeCartItem(id = CART_ID_1, price = PRICE_249, qty = CART_QTY_2)
        val entity = fakeCartItemEntity(CART_ID_1, MENU_ID_1, qty = CART_QTY_2)

        every { entityMapper.cartItemToEntity(cartItem) } returns entity
        coEvery { cartDao.insert(entity) } just runs

        createRepo().addItem(cartItem)

        coVerify { entityMapper.cartItemToEntity(cartItem) }
    }

    // ── updateQuantity ────────────────────────────────────────

    test("updateQuantity: calls cartDao.updateQuantity with correct params") {
        createRepo().updateQuantity(CART_ID_1, CART_QTY_3)

        coVerify { cartDao.updateQuantity(CART_ID_1, CART_QTY_3) }
    }

    test("updateQuantity: passes itemId and quantity correctly") {
        createRepo().updateQuantity(MENU_ID_1, CART_QTY_5)

        coVerify { cartDao.updateQuantity(MENU_ID_1, CART_QTY_5) }
    }

    // ── removeItem ────────────────────────────────────────────

    test("removeItem: calls cartDao.deleteById with correct itemId") {
        createRepo().removeItem(CART_ID_1)

        coVerify { cartDao.deleteById(CART_ID_1) }
    }

    test("removeItem: passes correct id — different item") {
        createRepo().removeItem(CART_ID_2)

        coVerify { cartDao.deleteById(CART_ID_2) }
    }

    // ── clearCart ─────────────────────────────────────────────

    test("clearCart: calls cartDao.clearAll") {
        createRepo().clearCart()
        coVerify { cartDao.clearAll() }
    }

    // ── itemExists ────────────────────────────────────────────

    test("itemExists: returns false when item not in cart") {
        coEvery { cartDao.getByMenuItemId(MENU_ID_1) } returns null

        val result = createRepo().itemExists(MENU_ID_1)

        result shouldBe false
    }

    test("itemExists: returns true when item exists in cart") {
        val entity = fakeCartItemEntity(CART_ID_1, MENU_ID_1, qty = CART_QTY_1)
        coEvery { cartDao.getByMenuItemId(MENU_ID_1) } returns entity

        val result = createRepo().itemExists(MENU_ID_1)

        result shouldBe true
    }

    test("itemExists: checks correct menuItemId") {
        val unknownId = "m_unknown"
        coEvery { cartDao.getByMenuItemId(unknownId) } returns null

        val result = createRepo().itemExists(unknownId)

        result shouldBe false
        coVerify { cartDao.getByMenuItemId(unknownId) }
    }

    // ── getCartTotal boundary + multi-item ────────────────────

    test("getCartTotal: delivery fee is 0 when subtotal exactly equals threshold") {
        val price = AppBusinessRules.FREE_DELIVERY_ABOVE
        val entity = fakeCartItemEntity(CART_ID_1, MENU_ID_1, qty = CART_QTY_1)
        val cartItem = fakeCartItem(id = CART_ID_1, price = price, qty = CART_QTY_1)

        every { cartDao.getAllItems() } returns flowOf(listOf(entity))
        every { entityMapper.cartItemToDomain(entity) } returns cartItem

        createRepo().getCartTotal().test {
            awaitItem().deliveryFee shouldBe 0.0
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCartTotal: multiple items subtotal summed correctly") {
        val entity1 = fakeCartItemEntity(CART_ID_1, MENU_ID_1, qty = CART_QTY_1)
        val entity2 = fakeCartItemEntity(CART_ID_2, MENU_ID_2, qty = CART_QTY_2)
        val cartItem1 = fakeCartItem(id = CART_ID_1, price = PRICE_249, qty = CART_QTY_1)
        val cartItem2 = fakeCartItem(id = CART_ID_2, price = PRICE_100, qty = CART_QTY_2)

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

    test("itemExists: uses getByMenuItemId not getById") {
        coEvery { cartDao.getByMenuItemId(MENU_ID_1) } returns
                fakeCartItemEntity(CART_ID_1, MENU_ID_1, qty = CART_QTY_1)

        val result = createRepo().itemExists(MENU_ID_1)

        result shouldBe true
        coVerify { cartDao.getByMenuItemId(MENU_ID_1) }
        coVerify(exactly = 0) { cartDao.getById(any()) }
    }
})