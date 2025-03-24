using System.Text.Json.Serialization;

namespace ReiAlgoAPI.Models.Post;

// 标记的子类会同时输出子类的属性
[JsonDerivedType(typeof(MarkdownContent), typeDiscriminator: "markdown")]
[JsonDerivedType(typeof(TipContent), typeDiscriminator: "tip")]
// 内容基类 通过枚举为不同的子类添加特有属性
public abstract class BaseBlogContent
{
    public abstract ContentType Type { get; }
}

