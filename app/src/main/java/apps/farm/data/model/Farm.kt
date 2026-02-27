package apps.farm.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "Farmtbl")
data class Farm(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val farmName: String,
    val farmNote: String = "",
    val blocked: Boolean = false
)
