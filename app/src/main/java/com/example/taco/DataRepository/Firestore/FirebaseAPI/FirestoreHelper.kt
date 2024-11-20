package com.example.taco.DataRepository.Firestore.FirebaseAPI

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

class FirestoreHelper {
    private val db = FirebaseFirestore.getInstance()

    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    fun uploadImageToStorage(imageUrl: String, onComplete: (String?) -> Unit) {
        val imageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")
        val uri = Uri.parse(imageUrl)  // Chuyển đổi chuỗi thành Uri

        imageRef.putFile(uri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    onComplete(downloadUri.toString()) // Trả về URL của hình ảnh đã upload
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    fun uploadImageToFirebase(
        context: Context,
        imageBitmap: Bitmap,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Tạo đường dẫn ngẫu nhiên cho ảnh trong Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference.child("images/${UUID.randomUUID()}.jpg")

        // Chuyển đổi ảnh Bitmap thành ByteArray
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()

        // Thực hiện tải ảnh lên Firebase Storage
        val uploadTask = storageRef.putBytes(imageData)
        uploadTask.addOnSuccessListener {
            // Lấy URL của ảnh sau khi tải lên thành công
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                onSuccess(uri.toString())  // Gọi callback với URL của ảnh
            }.addOnFailureListener { exception ->
                onFailure(exception)  // Gọi callback khi thất bại trong việc lấy URL
            }
        }.addOnFailureListener { exception ->
            // Gọi callback khi tải ảnh thất bại
            onFailure(exception)
        }
    }

    // Load image from Firebase Storage URL
    fun loadImageFromStorage(imageUrl: String, context: Context, onComplete: (Bitmap?) -> Unit) {

        val imageRef = storage.getReferenceFromUrl(imageUrl)
        val ONE_MEGABYTE: Long = 5 * 1024 * 1024

        imageRef.getBytes(ONE_MEGABYTE)

            .addOnSuccessListener { bytes ->
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                onComplete(bitmap)
            }
            .addOnFailureListener {
                onComplete(null)
            }

    }

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
    suspend fun loadImageFromUri(context: Context, uri: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val request = ImageRequest.Builder(context) // Giả định bạn có `context`
                    .data(uri)
                    .build()

                val result = ImageLoader(context).execute(request) as SuccessResult
                result.drawable.toBitmap() // Chuyển đổi Drawable thành Bitmap
            } catch (e: Exception) {
                e.printStackTrace()
                null // Trả về null nếu có lỗi
            }
        }
    }
    // Add Product
    fun addProduct(product: Product, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val productMap = hashMapOf(
            "name" to product.name,
            "price" to product.price,
            "oldPrice" to product.oldPrice,
            "image" to product.image
        )

        db.collection("Product").add(productMap)
            .addOnSuccessListener {
                onSuccess()  // Gọi hàm khi thêm sản phẩm thành công
            }
            .addOnFailureListener { exception ->
                onFailure(exception)  // Gọi hàm khi có lỗi
            }
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
    // Update Product with new image
    fun updateProductById(
        productId: String,
        updatedProduct: Product,
        newImageUri: String?
    ) {
        val productRef = db.collection("Product").document(productId)

        // Lấy thông tin sản phẩm hiện tại để kiểm tra xem có cần xóa ảnh cũ không
        productRef.get().addOnSuccessListener { document ->
            val oldImageUrl = document.getString("image")

            // Xóa ảnh cũ nếu có và có hình ảnh mới
            if (oldImageUrl != null && newImageUri != null) {
                FirebaseStorage.getInstance().getReferenceFromUrl(oldImageUrl).delete()
                    .addOnFailureListener {
                        Log.e("FirestoreHelper", "Không thể xoá ảnh cũ: $it")
                    }
            }

            // Nếu có ảnh mới, tải ảnh lên trước khi cập nhật
            if (newImageUri != null) {
                val newImageRef = storageRef.child("images/${UUID.randomUUID()}.jpg")
                newImageRef.putFile(Uri.parse(newImageUri)).addOnSuccessListener {
                    newImageRef.downloadUrl.addOnSuccessListener { uri ->
                        updatedProduct.image = uri.toString()
                        // Cập nhật sản phẩm với URL hình ảnh mới
                        productRef.set(updatedProduct.toMap())
                            .addOnSuccessListener { Log.d("FirestoreHelper", "Sản phẩm đã được cập nhật") }
                            .addOnFailureListener { Log.e("FirestoreHelper", "Cập nhật thất bại: $it") }
                    }
                }.addOnFailureListener {
                    Log.e("FirestoreHelper", "Tải ảnh mới lên thất bại: $it")
                }
            } else {
                // Nếu không có ảnh mới, chỉ cập nhật thông tin sản phẩm
                productRef.set(updatedProduct.toMap())
                    .addOnSuccessListener { Log.d("FirestoreHelper", "Sản phẩm đã được cập nhật") }
                    .addOnFailureListener { Log.e("FirestoreHelper", "Cập nhật thất bại: $it") }
            }
        }
    }


    private fun updateProductInFirestore(productId: String, updatedProductMap: Map<String, Any?>) {
        db.collection("Product").document(productId)
            .update(updatedProductMap)
            .addOnSuccessListener {
                println("Product updated successfully!")
            }
            .addOnFailureListener { e ->
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
            "isPayCheck" to orderProduct.isPayCheck,
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
                    isPayCheck = document.getBoolean("isPayCheck") ?: false,
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
    fun deleteProductById(productId: String, imageUrl: String, onComplete: (Boolean) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)

        // Xoá hình ảnh từ Firebase Storage
        storageRef.delete().addOnSuccessListener {
            // Sau khi xoá ảnh thành công, tiến hành xoá sản phẩm trong Firestore
            db.collection("Product").document(productId).delete()
                .addOnSuccessListener {
                    onComplete(true) // Xoá thành công
                }
                .addOnFailureListener { e ->
                    onComplete(false) // Xoá sản phẩm thất bại
                    Log.e("Firestore", "Error deleting product document", e)
                }
        }.addOnFailureListener { e ->
            onComplete(false) // Xoá hình ảnh thất bại
            Log.e("FirebaseStorage", "Error deleting image", e)
        }
    }


    suspend fun updateOrderProductByFilterList(orderProductList: List<OrderProduct>, isPayCheck: Boolean) {
        val db = FirebaseFirestore.getInstance()

        orderProductList.forEach { orderProduct ->
            val orderProductRef = db.collection("OrderProduct").document(orderProduct.orderId)

            // Kiểm tra xem tài liệu có tồn tại không
            val snapshot = orderProductRef.get().await()
            if (snapshot.exists()) {
                // Cập nhật trường isPayCheck
                orderProductRef.update("isPayCheck", isPayCheck).await()
            } else {
                Log.d("Firestore", "Tài liệu không tồn tại: ${orderProduct.orderId}")
            }
        }
    }


    fun updateOrderProductById(orderProductId: String, updatedOrderProduct: OrderProduct) {
        // Tạo một map chứa các trường cần cập nhật
        val updatedOrderProductMap = hashMapOf<String, Any?>(
            "productId" to updatedOrderProduct.productId,
            "cusName" to updatedOrderProduct.cusName,
            "phoneNumber" to updatedOrderProduct.phoneNumber,
            "isProblem" to updatedOrderProduct.isProblem,
            "quantity" to updatedOrderProduct.quantity,
            "totalPrice" to updatedOrderProduct.totalPrice,
            "note" to updatedOrderProduct.note,
            "orderDone" to updatedOrderProduct.orderDone,
            "startOrderTime" to updatedOrderProduct.startOrderTime,
            "orderCompletedTime" to updatedOrderProduct.orderCompletedTime
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
    suspend fun deleteOrderProductsByPhoneNumber(phoneNumber: String) {
        try {
            // Truy vấn các orderProduct có phoneNumber khớp
            val snapshot = db.collection("OrderProduct")
                .whereEqualTo("phoneNumber", phoneNumber)
                .get()
                .await()

            // Xóa từng orderProduct dựa trên orderId
            for (document in snapshot.documents) {
                val orderId = document.id
                db.collection("OrderProduct").document(orderId).delete().await()
            }

            println("Successfully deleted all orders for phone number: $phoneNumber")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error deleting orders for phone number: $phoneNumber - $e")
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
    var name: String = "",
    var price: Double = 0.0,
    var oldPrice: Double? = null,
    var image: String? = null // Thay đổi từ ByteArray thành String (URL hoặc base64)
){
    fun toMap() = hashMapOf("name" to name, "price" to price, "oldPrice" to oldPrice, "image" to image)
}

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
    val isPayCheck : Boolean = false
)


data class Customer (
    val id: String = "",
    val cusName: String = "",
    val phoneNumber: String = ""
)

data class Revenue(
    val revenueId: String = "",
    val revenueDate: Date = Date(),
    val orderId: String = "",
    val cusName: String = "",
    val cusPhone: String = "",
    val totalPrice: Double = 0.0
)

data class HOrderProduct(
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
    val isPayCheck : Boolean = false
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
