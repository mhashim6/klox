 My Kotlin interpreter for the Lox Programming Language.  
  following the amazing book [Crafting Interpreters](http://craftinginterpreters.com/).

 
**About Lox:**
Lox is an Object Oriented Dynamic Scripting language with Higher-Order Functions support, Designed by [Bob Nystrom](https://github.com/munificent).

---
 What makes this implementation different than other Kotlin implementations *(aside from my messy git log)*, is that it doesn't stick to the book in terms of design patterns;    
 
I tried to embrace functional Kotlin in this project, yet (unfortunately) I've not even come close to pure Functional Programming principles.  
  
The thing is, having mutable global state floating around each pass in the compiler is extremely inconvenient to maintain/debug.    

 In the `broken_immutable_state` branch you'll find my *obviously-broken* attempt at immutable state with Kotlin's implementation of [persistent data structures](https://github.com/Kotlin/kotlinx.collections.immutable).   
But without implicit parameters support it was really painful to implement. 
  
However, I didn't completely give up on FP with Kotlin; the Lexer, Parser, Resolver, and Interpreter are all functions.   Recursion and pattern-matching *(as limited as kotlin's support for it)* are heavily used, and I plan to refactor the entire project to render it as functional as possible.  
  
I did consider using [ΛRROW](https://arrow-kt.io/), but so far it feels to me like a language inside the language. Perhaps Kotlin is not ready yet to be a pure functional language *(on it's own)*?  
*roll news-report music*
