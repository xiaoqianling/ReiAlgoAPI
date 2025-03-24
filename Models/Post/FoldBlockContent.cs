using System.Text.Json.Serialization;

namespace ReiAlgoAPI.Models.Post;

public class FoldBlockContent : BaseBlogContent
{
    [JsonPropertyName("type")]
    public override ContentType Type => ContentType.Fold;
    [JsonPropertyName("title")]
    public required string Title { get; set; }
    [JsonPropertyName("content")]
    public required string Content { get; set; }

}