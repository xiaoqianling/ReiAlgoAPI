using System.Text.Json;
using Microsoft.AspNetCore.Cors;
using Microsoft.AspNetCore.Mvc;
using ReiAlgoAPI.Models.Post;

namespace ReiAlgoAPI.Controllers;

[ApiController]
[Route("api/[controller]")]
[EnableCors("DevCors")]
public class PostController : ControllerBase
{
  [HttpGet("{postId}/contents")]
  public IActionResult GetPostContents(string postId, [FromQuery] int page = 1)
  {
    var post = new Post
    {
      Id = "mock-id-2",
      Title = "深入理解React Hooks：从基础到高级用法",
      Username = "react-expert",
      UserLink = "/user/react-expert",
      Contents = new List<BaseBlogContent>
      {
        new MarkdownContent
        {
          Content = @"# React Hooks 全面指南
                        ## 什么是React Hooks？
                        React Hooks是React 16.8引入的新特性...",
        },
        new TipContent
        {
          Content = "使用Hooks时，请确保遵守Hooks的规则，特别是在条件语句和循环中。",
          Level = TipLevel.Tip
        },
        // 其他内容项...
      },
      CreatedAt = new DateTime(2023, 1, 1),
      UpdatedAt = DateTime.Now,
      Tags = new List<TagType> { TagType.Tech }
    };

    var json = JsonSerializer.Serialize(post);
    Console.WriteLine(json);
    return Ok(json);
  }
}

