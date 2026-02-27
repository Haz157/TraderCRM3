package apps.farm.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "safetbl")
data class Safe(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val balance: Double = 0.0,
    val note: String = "",
    val blocked: Boolean = false
)
