namespace ReiAlgoAPI.Models.Post;

public class MarkdownContent : BaseBlogContent
{
    public override ContentType Type => ContentType.Markdown;
    public string Content { get; set; }
}