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
    CODE,
    [JsonStringEnumMemberName("tip")]
    Tip,
  }

  // 添加其他需要的标签类型 TODO: 支持用户添加标签分类
  // post的分类
  public enum TagType
  {
    Tech,
  }

  public class Post
  {
    [JsonPropertyName("id123456")]
    public string Id { get; set; }
    [JsonPropertyName("title")]
    public string Title { get; set; }
    [JsonPropertyName("username")]
    public string Username { get; set; }
    [JsonPropertyName("userLink")]
    public string UserLink { get; set; }
    [JsonPropertyName("contents")]
    public List<BaseBlogContent> Contents { get; set; }
    [JsonPropertyName("createdAt")]
    public DateTime CreatedAt { get; set; }
    [JsonPropertyName("updatedAt")]
    public DateTime UpdatedAt { get; set; }
    [JsonPropertyName("tags")]
    public List<TagType> Tags { get; set; }
  }
}
