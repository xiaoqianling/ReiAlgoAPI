using System.Text.Json.Serialization;

namespace ReiAlgoAPI.Models.Post
{
  [JsonConverter(typeof(JsonStringEnumConverter))]
  // 每个组件块类型
  public enum ContentType
  {
    [JsonStringEnumMemberName("markdown")]
    Markdown,
    [JsonStringEnumMemberName("code")]
    Code,
    [JsonStringEnumMemberName("tip")]
    Tip,
    [JsonStringEnumMemberName("fold")]
    Fold,
  }

  // 添加其他需要的标签类型 TODO: 支持用户添加标签分类
  // post的分类
  [JsonConverter(typeof(JsonStringEnumConverter))]
  public enum TagType
  {
    [JsonStringEnumMemberName("tech")]
    Tech,
  }

  public class Post
  {
    [JsonPropertyName("id")]
    public required string Id { get; set; }
    [JsonPropertyName("title")]
    public required string Title { get; set; }
    [JsonPropertyName("username")]
    public required string Username { get; set; }
    [JsonPropertyName("userLink")]
    public string? UserLink { get; set; }
    [JsonPropertyName("contents")]
    public required List<BaseBlogContent> Contents { get; set; }
    [JsonPropertyName("createdAt")]
    public required DateTime CreatedAt { get; set; }
    [JsonPropertyName("updatedAt")]
    public required DateTime UpdatedAt { get; set; }
    [JsonPropertyName("tags")]
    public List<TagType>? Tags { get; set; }
  }
}
