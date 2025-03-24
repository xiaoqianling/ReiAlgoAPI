using System.Text.Json.Serialization;

namespace ReiAlgoAPI.Models.Post;

public class TipContent : BaseBlogContent
{
    public override ContentType Type => ContentType.Tip;
    public TipLevel Level { get; set; }
    public string Content { get; set; }
}


[JsonConverter(typeof(JsonStringEnumConverter))]
// Tip组件的级别类型
public enum TipLevel
{
    [JsonStringEnumMemberName("tip")]
    Tip,
    [JsonStringEnumMemberName("warn")]
    Warning,
    [JsonStringEnumMemberName("error")]
    Error
}