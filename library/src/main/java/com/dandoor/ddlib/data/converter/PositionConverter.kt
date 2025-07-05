import androidx.room.TypeConverter
import com.dandoor.ddlib.data.entity.Position
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PositionConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromPositionList(value: List<Position>): String = gson.toJson(value)

    @TypeConverter
    fun toPositionList(value: String): List<Position> {
        val listType = object : TypeToken<List<Position>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromPosition(position: Position): String = gson.toJson(position)

    @TypeConverter
    fun toPosition(value: String): Position = gson.fromJson(value, Position::class.java)
}