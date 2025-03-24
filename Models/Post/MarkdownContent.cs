using System.Text.Json.Serialization;

namespace ReiAlgoAPI.Models.Post;

public class MarkdownContent : BaseBlogContent
{
    [JsonPropertyName("type")]
    public override ContentType Type => ContentType.Markdown;
    [JsonPropertyName("content")]
    public string Content { get; set; }
}