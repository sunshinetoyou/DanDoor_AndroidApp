import androidx.room.TypeConverter
import com.dandoor.ddlib.data.entity.Position
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room DB는 기본 형식 ( Int, String, Bool, ... ) 만 저장이 가능함.
 *
 * 하지만 좌표 표현을 위한 Position 객체(커스텀 객체)를 다루기 위해
 * TypeConverter를 구현해서 저장 시에는 문자열 객체로, 불러올 때는 원래 타입으로 복원하여 가져올 수 있게 한다.
 *
 * 한줄 요약 : 커스텀 객체 (Position)을 room Db 내에서 처리하기 위해 Converter를 정의하여 사용한다.
 */
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