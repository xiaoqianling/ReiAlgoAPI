using System.Text.Json;
using System.Text.Json.Serialization;

namespace reiAlgoAPI.Util.JsonConverters;

// 后端对枚举量映射到前端string
public class Enum2StringConverter<T> : JsonConverter<T> where T : struct, Enum
{
  // JSON -> T object
  public override T Read(ref Utf8JsonReader reader, Type typeToConvert, JsonSerializerOptions options)
  {
    return Enum.Parse<T>(reader.GetString(), ignoreCase: true);
  }

  // Object -> JSON
  public override void Write(Utf8JsonWriter writer, T value, JsonSerializerOptions options)
  {
    writer.WriteStringValue(value.ToString().ToLower());
  }
}
