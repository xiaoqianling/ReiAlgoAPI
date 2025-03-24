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
            Contents =
            [
                new MarkdownContent
                {
                    Content = @"
# React Hooks 全面指南

## 什么是React Hooks？
React Hooks是React 16.8引入的新特性，它允许你在函数组件中使用state和其他React特性。

## 基础Hooks

### useState
```jsx
function Counter() {
  const [count, setCount] = useState(0);

  return (
    <div>
      <p>You clicked {count} times</p>
      <button onClick={() => setCount(count + 1)}>
        Click me
      </button>
    </div>
  );
}
```
### useEffect
```jsx
function Example() {
  const [data, setData] = useState(null);

  useEffect(() => {
    fetch('/api/data')
      .then(res => res.json())
      .then(data => setData(data));
  }, []); // 空数组表示只在组件挂载时执行

  return <div>{data ? data.message : 'Loading...'}</div>;
}
```
## 高级用法

### 自定义Hook
```javascript
function useWindowSize() {
  const [size, setSize] = useState({
    width: window.innerWidth,
    height: window.innerHeight
  });

  useEffect(() => {
    const handleResize = () => setSize({
      width: window.innerWidth,
      height: window.innerHeight
    });
    
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  return size;
}
```

## 性能优化

### useMemo
```jsx
function ExpensiveComponent({ a, b }) {
  const result = useMemo(() => {
    // 复杂计算
    return a * b;
  }, [a, b]);

  return <div>{result}</div>;
}
```

### useCallback
```jsx
function ParentComponent() {
  const [count, setCount] = useState(0);

  const increment = useCallback(() => {
    setCount(c => c + 1);
  }, []);

  return <ChildComponent onClick={increment} />;
}
```
",
                },
                new TipContent
                {
                    Content = "使用Hooks时，请确保遵守Hooks的规则，特别是在条件语句和循环中。",
                    Level = TipLevel.Tip
                },
                new TipContent
                {
                    Content = "使用Hooks时，请确保遵守Hooks的规则，特别是在条件语句和循环中。",
                    Level = TipLevel.Warning
                },
                new TipContent
                {
                    Content = "使用Hooks时，请确保遵守Hooks的规则，特别是在条件语句和循环中。",
                    Level = TipLevel.Error
                },
                new CodeContent
                {
                    Metadata =
                    [
                        new CodeBlock()
                        {
                            Language = "javascript",
                            Code = "const [state, setState] = useState(initialState);"
                        },
                        new CodeBlock()
                        {
                            Language = "typescript",
                            Code = "const [state, setState] = useState<Type>(initialState);"
                        }
                    ]
                },
                new FoldBlockContent()
                {
                    Title = "测试标题",
                    Content = "测试文本"
                }
                // 其他内容项...
            ],
            CreatedAt = new DateTime(2023, 1, 1),
            UpdatedAt = DateTime.Now,
            Tags = [TagType.Tech]
        };

        var json = JsonSerializer.Serialize(post);
        Console.WriteLine(json);
        return Ok(json);
    }
}