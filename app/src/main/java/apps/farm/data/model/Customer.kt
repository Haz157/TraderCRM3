package apps.farm.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "Customertbl")
data class Customer(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val balance: Double = 0.0,
    val phone: String = "",
    val address: String = "",
    val note: String = "",
    val invoiceId: String = "",
    val blocked: Boolean = false
)
