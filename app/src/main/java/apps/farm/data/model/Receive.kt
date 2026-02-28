package apps.farm.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    tableName = "receivetbl",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Safe::class,
            parentColumns = ["id"],
            childColumns = ["safeId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["safeId"])
    ]
)
data class Receive(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val customerId: String,
    val safeId: String,
    val receive: Double,
    val discount: Double = 0.0,
    val receiveNo: Int = 0,
    val note: String = "",
    val createdDate: Long = System.currentTimeMillis(),
    val isBlocked: Boolean = false
)
