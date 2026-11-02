package com.vlwtextile.inventory.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlwtextile.inventory.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed class UserRole {
    object None : UserRole()
    object Employee : UserRole()
    object Owner : UserRole()
}

class InventoryViewModel(private val dao: InventoryDao) : ViewModel() {

    private val _currentRole = MutableStateFlow<UserRole>(UserRole.None)
    val currentRole: StateFlow<UserRole> = _currentRole

    private val _scannedProduct = MutableStateFlow<ProductEntity?>(null)
    val scannedProduct: StateFlow<ProductEntity?> = _scannedProduct

    private val _saleResult = MutableStateFlow<String?>(null)
    val saleResult: StateFlow<String?> = _saleResult

    fun login(pin: String) {
        if (pin == "1234") {
            _currentRole.value = UserRole.Employee
        } else if (pin == "8888") {
            _currentRole.value = UserRole.Owner
        }
    }

    fun logout() {
        _currentRole.value = UserRole.None
        _scannedProduct.value = null
    }

    fun scanProductBarcode(barcode: String) {
        viewModelScope.launch {
            _scannedProduct.value = dao.getProductByBarcode(barcode)
        }
    }

    fun submitEmployeeSale(
        product: ProductEntity,
        employeeId: String,
        soldQty: Int,
        sellingPrice: Double,
        discount: Double,
        paymentMode: String
    ) {
        viewModelScope.launch {
            if (sellingPrice < product.minSellingPrice) {
                _saleResult.value = "Error: Price is below Owner's Minimum Limit!"
                return@launch
            }
            if (soldQty > product.availableQuantity) {
                _saleResult.value = "Error: Insufficient Stock!"
                return@launch
            }

            val totalAmount = (soldQty * sellingPrice) - discount
            val transactionId = "TXN-" + UUID.randomUUID().toString().take(8)

            val rowsUpdated = dao.deductStock(product.productId, soldQty)
            if (rowsUpdated > 0) {
                val sale = SaleEntity(
                    transactionId = transactionId,
                    productId = product.productId,
                    employeeId = employeeId,
                    soldQuantity = soldQty,
                    sellingPricePerPiece = sellingPrice,
                    discount = discount,
                    totalSaleAmount = totalAmount,
                    paymentMode = paymentMode,
                    timestamp = System.currentTimeMillis()
                )
                dao.insertSale(scale)

                val log = ActivityLogEntity(
                    activityId = UUID.randomUUID().toString(),
                    employeeId = employeeId,
                    actionType = "SALE_SUBMITTED",
                    productId = product.productId,
                    details = "Qty: $soldQty, Total: ₹$totalAmount, Mode: $paymentMode",
                    timestamp = System.currentTimeMillis()
                )
                dao.insertActivityLog(log)

                _saleResult.value = "Success: $transactionId"
            } else {
                _saleResult.value = "Error: Stock Update Failed!"
            }
        }
    }

    fun reportOutOfStock(product: ProductEntity, employeeId: String) {
        viewModelScope.launch {
            val alert = StockAlertEntity(
                reportId = "ALT-" + UUID.randomUUID().toString().take(8),
                productId = product.productId,
                employeeId = employeeId,
                status = "PENDING",
                timestamp = System.currentTimeMillis()
            )
            dao.insertStockAlert(alert)
        }
    }
}
