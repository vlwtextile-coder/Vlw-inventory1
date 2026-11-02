package com.vlwtextile.inventory.ui

import androidx.compose.foundation.background
import androidx.compose.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import 

    Surface(modifier = Modifier.fillMaxSize(), color = PremiumOffWhite) {
        when (role) {
            is UserRole.None -> LoginScreen(onLogin = { pin -> viewModel.login(pin) })
            is UserRole.Employee -> EmployeeHomeScreen(viewModel)
            is UserRole.Owner -> OwnerDashboardScreen(viewModel)
        }
    }
}

@Composable
fun LoginScreen(onLogin: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("VLW TEXTILE", style = MaterialTheme.typography.headlineLarge, color = PremiumDarkGreen)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Offline Inventory System", color = PremiumGold)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it },
            label = { Text("Enter Security PIN") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onLogin(pin) },
            colors = ButtonDefaults.buttonColors(containerColor = PremiumDarkGreen)
        ) {
            Text("LOGIN", color = Color.White)
        }
    }
}

@Composable
fun EmployeeHomeScreen(viewModel: InventoryViewModel) {
    val product by viewModel.scannedProduct.collectAsState()
    val saleResult by viewModel.saleResult.collectAsState()

    var soldQty by remember { mutableStateOf("1") }
    var actualPrice by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("0") }
    var paymentMode by remember { mutableStateOf("CASH") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Employee Mode", style = MaterialTheme.typography.titleLarge, color = PremiumDarkGreen)
            TextButton(onClick = { viewModel.logout() }) { Text("Logout", color = Color.Red) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.scanProductBarcode("890123456789") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PremiumDarkGreen)
        ) {
            Text("SCAN BARCODE")
        }

        product?.let { prod ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Product: ${prod.name}", style = MaterialTheme.typography.titleMedium)
                    Text("SKU: ${prod.sku} | Available: ${prod.availableQuantity} Pcs")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = soldQty,
                onValueChange = { soldQty = it },
                label = { Text("Sold Quantity") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = actualPrice,
                onValueChange = { actualPrice = it },
                label = { Text("Selling Price / Piece (₹)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = discount,
                onValueChange = { discount = it },
                label = { Text("Discount (₹)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            val qtyInt = soldQty.toIntOrNull() ?: 0
            val priceDbl = actualPrice.toDoubleOrNull() ?: 0.0
            val discDbl = discount.toDoubleOrNull() ?: 0.0
            val calculatedTotal = (qtyInt * priceDbl) - discDbl

            Text(
                "Actual Total Sale: ₹$calculatedTotal",
                style = MaterialTheme.typography.headlineSmall,
                color = PremiumDarkGreen
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.submitEmployeeSale(
                        product = prod,
                        employeeId = "EMP-01",
                        soldQty = qtyInt,
                        sellingPrice = priceDbl,
                        discount = discDbl,
                        paymentMode = paymentMode
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("CONFIRM & SUBMIT SALE")
            }
        }

        saleResult?.let { msg ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(msg, color = if (msg.startsWith("Success")) Color.Green else Color.Red)
        
    
