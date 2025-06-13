### Recursive-Descent Syntax Analyser 

A lightweight Java project that implements the **syntax-analysis phase of a tiny Ada-like language compiler**.
It uses a hand-written recursive-descent parser to recognise the grammar supplied in the coursework brief and reports the **first syntax error with contextual, line-accurate messages**.&#x20;

**Key points**

* **`SyntaxAnalyser.java`** – concrete subclass of `AbstractSyntaxAnalyser`; drives the parse over `<StatementPart>` and its subordinate non-terminals.
* **`Generate.java`** – concrete subclass of `AbstractGenerate`; prints a readable trace (`commenceNonterminal`, `insertTerminal`, `finishNonterminal`) and raises `CompilationException` on error.&#x20;
* Leverages the provided **`Token`, `LexicalAnalyser`, and `Compile`** classes; **no changes** to these files are required (and will break assessment scripts).&#x20;

**Build & run**

```bash
# Linux / macOS
make          # compile all sources
make run      # run parser over the supplied test programs
make package  # create submission ZIP (optional)

:: Windows
compile.bat   REM compile all sources
execute.bat   REM run parser over test programs
```

Successful parses end with `reportSuccess()`. On the first syntax error the analyser halts and prints a detailed message showing the unexpected token, its line number, and the non-terminal in which it occurred.&#x20;

---
