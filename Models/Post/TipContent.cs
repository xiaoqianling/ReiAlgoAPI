using System.Text.Json.Serialization;

namespace ReiAlgoAPI.Models.Post;

public class TipContent : BaseBlogContent
{
    [JsonPropertyName("type")]
    public override ContentType Type => ContentType.Tip;
    [JsonPropertyName("level")]
    public TipLevel Level { get; set; }
    [JsonPropertyName("content")]
    public required string Content { get; set; }
}


[JsonConverter(typeof(JsonStringEnumConverter))]
// Tip组件的级别类型
public enum TipLevel
{
    [JsonStringEnumMemberName("tip")]
    Tip,
    [JsonStringEnumMemberName("warning")]
    Warning,
    [JsonStringEnumMemberName("error")]
    Error
}