package com.example.taco.DataRepository.Firestore.FirebaseAPI

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.type.DateTime
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.util.Date
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

class FirestoreHelper {

    private val db = FirebaseFirestore.getInstance()

    // Add Customer
    fun addCustomer(customer: Customer) {
        val customerMap = hashMapOf(
            "cusName" to customer.cusName,
            "phoneNumber" to customer.phoneNumber
        )
        db.collection("Customer").add(customerMap)
            .addOnSuccessListener {
                // Thành công
                println("Customer added successfully!")
            }
            .addOnFailureListener { e ->
                // Thất bại
                println("Error adding customer: $e")
            }
    }

    // Get Customer by ID
    suspend fun getCustomerById(customerId: String): Customer? {
        return try {
            val document = db.collection("Customer").document(customerId).get().await()
            if (document.exists()) {
                Customer(
                    cusName = document.getString("cusName") ?: "",
                    phoneNumber = document.getString("phoneNumber") ?: ""
                )
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Get All Customers
    suspend fun getAllCustomers(): List<Customer> {
        val customers = mutableListOf<Customer>()
        try {
            val querySnapshot = db.collection("Customer").get().await()
            for (document in querySnapshot.documents) {
                val customer = Customer(
                    cusName = document.getString("cusName") ?: "",
                    phoneNumber = document.getString("phoneNumber") ?: ""
                )
                customers.add(customer)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return customers
    }

    // Hàm cập nhật thông tin khách hàng theo ID
    fun updateCustomerById(customerId: String, updatedCustomer: Customer) {
        // Tạo một map chứa các trường cần cập nhật
        val updatedCustomerMap = hashMapOf<String, Any>(
            "cusName" to updatedCustomer.cusName,
            "phoneNumber" to updatedCustomer.phoneNumber
        )

        // Cập nhật dữ liệu cho khách hàng với customerId trong Firestore
        db.collection("Customer").document(customerId)
            .update(updatedCustomerMap)
            .addOnSuccessListener {
                // Thành công
                println("Customer updated successfully!")
            }
            .addOnFailureListener { e ->
                // Thất bại
                println("Error updating customer: $e")
            }
    }


    // Delete Customer by ID
    fun deleteCustomerById(customerId: String) {
        db.collection("Customer").document(customerId).delete()
            .addOnSuccessListener {
                println("Customer deleted successfully!")
            }
            .addOnFailureListener { e ->
                println("Error deleting customer: $e")
            }
    }

    // Delete All Customers
    fun deleteAllCustomers() {
        db.collection("Customer").get().addOnSuccessListener { result ->
            for (document in result) {
                db.collection("Customer").document(document.id).delete()
            }
        }
    }



    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedString = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }


    // Add Product
    fun addProduct(product: Product) {
        val productMap = hashMapOf(
            "name" to product.name,
            "price" to product.price,
            "oldPrice" to product.oldPrice,
            "image" to product.image
        )
        db.collection("Product").add(productMap)
    }
    fun addAccount(account: Account){
        val accountMap = hashMapOf(
            "accountName" to account.accountName,
            "accountUsername" to account.accountUsername,
            "accountPassword" to account.accountPassword,
            "accountRole" to account.accountRole,
            "isAdmin" to account.isAdmin
        )
        db.collection("Account").add((accountMap))
    }

    suspend fun getAccountById(accountId: String): Account? {
        val document = db.collection("Account").document(accountId).get().await()
        return if (document.exists()) {
            Account(
                accountId = document.id,
                accountName = document.getString("accountName") ?: "",
                accountUsername = document.getString("accountUsername") ?: "",
                accountPassword = document.getString("accountPassword") ?: "",
                accountRole = document.getString("accountRole") ?: "",
                isAdmin = document.getBoolean("isAdmin") ?: false
            )
        } else null
    }

    suspend fun getAllAccounts(): List<Account> {
        val accounts = mutableListOf<Account>()
        try {
            val querySnapshot = db.collection("Account").get().await()
            for (document in querySnapshot.documents) {
                val account = Account(
                    accountId = document.id,
                    accountName = document.getString("accountName") ?: "",
                    accountUsername = document.getString("accountUsername") ?: "",
                    accountPassword = document.getString("accountPassword") ?: "",
                    accountRole = document.getString("accountRole") ?: "",
                    isAdmin = document.getBoolean("isAdmin") ?: false
                )
                accounts.add(account)
            }
        } catch (e: Exception) {
            // Xử lý lỗi nếu có vấn đề xảy ra trong quá trình lấy dữ liệu
            e.printStackTrace()
        }
        return accounts
    }

    // Get All Products
    suspend fun getAllProducts(): List<Product> {
        val snapshot = db.collection("Product").get().await()
        return snapshot.documents.map { document ->
            Product(
                productId = document.id,
                name = document.getString("name") ?: "",
                price = document.getDouble("price") ?: 0.0,
                oldPrice = document.getDouble("oldPrice"),
                image = document.getString("image") // Thay đổi từ getBlob thành getString
            )
        }
    }

    // Get Product by ID
    suspend fun getProductById(productId: String): Product? {
        val document = db.collection("Product").document(productId).get().await()
        return if (document.exists()) {
            Product(
                productId = document.id,
                name = document.getString("name") ?: "",
                price = document.getDouble("price") ?: 0.0,
                oldPrice = document.getDouble("oldPrice"),
                image = document.getString("image") // Thay đổi từ getBlob thành getString
            )
        } else null
    }
    fun updateProductById(productId: String, updatedProduct: Product) {
        // Tạo một map chứa các trường cần cập nhật
        val updatedProductMap = hashMapOf<String, Any?>(
            "name" to updatedProduct.name,
            "price" to updatedProduct.price,
            "oldPrice" to updatedProduct.oldPrice,
            "image" to updatedProduct.image
        )

        // Cập nhật dữ liệu cho sản phẩm với productId trong Firestore
        db.collection("Product").document(productId)
            .update(updatedProductMap)
            .addOnSuccessListener {
                // Thành công
                println("Product updated successfully!")
            }
            .addOnFailureListener { e ->
                // Thất bại
                println("Error updating product: $e")
            }
    }

    // Add OrderProduct
    suspend fun addOrderProduct(orderProduct: OrderProduct, onComplete: (DocumentReference) -> Unit) {
        val orderProductMap = hashMapOf(
            "productId" to orderProduct.productId,
            "cusName" to orderProduct.cusName,
            "phoneNumber" to orderProduct.phoneNumber,
            "isProblem" to orderProduct.isProblem,
            "quantity" to orderProduct.quantity,
            "totalPrice" to orderProduct.totalPrice,
            "note" to orderProduct.note,
            "orderDone" to orderProduct.orderDone,
            "startOrderTime" to orderProduct.startOrderTime,
            "orderCompletedTime" to orderProduct.orderCompletedTime
        )

        val documentReference = db.collection("OrderProduct").add(orderProductMap).await()
        onComplete(documentReference)
    }

    suspend fun getAllOrderProducts(): List<OrderProduct> {
        val orderProducts = mutableListOf<OrderProduct>()
        try {
            val querySnapshot = db.collection("OrderProduct").get().await()
            for (document in querySnapshot.documents) {
                val orderProduct = OrderProduct(
                    orderId = document.id,
                    productId = document.getString("productId") ?: "",
                    cusName = document.getString("tableId") ?: "",
                    phoneNumber = document.getString("phoneNumber") ?: "",
                    isProblem = document.getBoolean("isProblem") ?: false,
                    quantity = document.getLong("quantity")?.toInt() ?: 0,
                    totalPrice = document.getDouble("totalPrice") ?: 0.0,
                    note = document.getString("note") ?: "",
                    orderDone = document.getBoolean("orderDone") ?: false,
                    startOrderTime = document.getDate("startOrderTime") ?: Date(),
                    orderCompletedTime = document.getDate("orderCompletedTime") ?: Date(),
                )
                orderProducts.add(orderProduct)
            }
        } catch (e: Exception) {
            e.printStackTrace() // Xử lý lỗi nếu có
        }
        return orderProducts
    }

    // Get Order Products by Order ID
    suspend fun getOrderProductsByOrderId(orderId: String): List<OrderProduct> {
        val snapshot = db.collection("OrderProduct")
            .whereEqualTo("orderId", orderId)
            .get()
            .await()
        return snapshot.documents.map { document ->
            OrderProduct(
                orderId = document.getString("orderId") ?: "",
                productId = document.getString("productId") ?: "",
                quantity = document.getLong("quantity")?.toInt() ?: 0,
                totalPrice = document.getDouble("totalPrice") ?: 0.0
            )
        }
    }

    // Delete Product by ID
    fun deleteProductById(productId: String) {
        db.collection("Product").document(productId).delete()
    }

    fun updateOrderProductById(orderProductId: String, updatedOrderProduct: OrderProduct) {
        // Tạo một map chứa các trường cần cập nhật
        val updatedOrderProductMap = hashMapOf<String, Any?>(
            "productId" to updatedOrderProduct.productId,
            "quantity" to updatedOrderProduct.quantity,
            "cusName" to updatedOrderProduct.cusName,
            "phoneNumber" to updatedOrderProduct.phoneNumber,
            "isProblem" to updatedOrderProduct.isProblem,
            "totalPrice" to updatedOrderProduct.totalPrice,
            "note" to updatedOrderProduct.note
        )

        // Cập nhật dữ liệu cho OrderProduct với orderProductId trong Firestore
        db.collection("OrderProduct").document(orderProductId)
            .update(updatedOrderProductMap)
            .addOnSuccessListener {
                // Thành công
                println("OrderProduct updated successfully!")
            }
            .addOnFailureListener { e ->
                // Thất bại
                println("Error updating OrderProduct: $e")
            }
    }
    suspend fun deleteOrderProductById(orderId: String) {
        try {
            val documentRef = db.collection("OrderProduct").document(orderId)
            documentRef.delete().await() // Gọi phương thức delete để xóa tài liệu
        } catch (e: Exception) {
            e.printStackTrace() // Xử lý lỗi nếu có
        }
    }



    // Delete All Orders
    fun deleteAllOrders() {
        db.collection("OrderProduct").get().addOnSuccessListener { result ->
            for (document in result) {
                db.collection("OrderProduct").document(document.id).delete()
            }
        }
    }




}

data class Product(
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val oldPrice: Double? = null,
    val image: String? = null // Thay đổi từ ByteArray thành String (URL hoặc base64)
)

data class OrderProduct(
    val orderId: String = "",
    val productId: String = "",
    val cusName: String = "",
    val phoneNumber: String = "",
    val isProblem: Boolean = false,
    val quantity: Int = 0,
    val totalPrice: Double = 0.0,
    val note: String = "",
    val orderDone: Boolean = false,
    val startOrderTime: Date = Date(),
    val orderCompletedTime: Date = Date(),
)

data class Customer (
    val id: String = "",
    val cusName: String = "",
    val phoneNumber: String = ""
)



data class Revenue(
    val revenueId: String = "",
    val revenueDate: Date = Date(),
    val tableId: String = "",
    val totalRevenue: Double = 0.0
)

data class Feedback(
    val feedbackId: String = "",
    val feedbackDate: Date = Date(),
    val feedbackContent: String = "",
    val feedbackRating: Int = 0,
    val feedbackName: String = "",
    val feedbackEmail: String = "",
    val feedbackPhone: String = "",
    val feedbackStatus: Boolean = false,
    val feedbackResponseDate: Date = Date(),
    val feedbackResponseName: String = "",
    val feedbackResponseEmail: String = "",
    val feedbackResponsePhone: String = "",
    val feedbackResponseStatus: Boolean = false,
    val feedbackResponseContent: String = "",
    val feedbackResponseRating: Int = 0,
    val feedbackResponseId: String = "",
)

data class Account(
    var accountId: String = "",
    var accountName: String = "",
    var accountUsername: String = "", // Admin Account
    var accountPassword: String = "",
    var accountRole: String = "",
    var isAdmin: Boolean = false
)