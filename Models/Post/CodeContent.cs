using System.Text.Json.Serialization;

namespace ReiAlgoAPI.Models.Post;

public class CodeContent : BaseBlogContent
{
    [JsonPropertyName("type")]
    public override ContentType Type => ContentType.Code;
    [JsonPropertyName("metadata")]
    public required List<CodeBlock> Metadata { get; set; }
}

public class CodeBlock
{
    [JsonPropertyName("language")]
    public required string Language { get; set; }
    [JsonPropertyName("code")]
    public required string Code { get; set; }
}
