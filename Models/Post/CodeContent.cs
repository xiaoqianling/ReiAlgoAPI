namespace ReiAlgoAPI.Models.Post;

public class CodeContent : BaseBlogContent
{
    public override ContentType Type => ContentType.CODE;
    public List<(string Language, string Content)> Metadata { get; set; }
}