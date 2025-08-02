#NLQ-GC

This Java Spring Boot application provides a service to transform natural language queries (NLQs) into graph codes (GCs) by using large language models (LLMs).

## Expanding the project

To add another LLM you simply create a class in the corresponding llm package extending the abstract class `LanguageModel`. 
In this class you implement methods to connect to an external LLM service. 
Possibly you need to implement a transaction management in case your LLM does not provide a suitable one.
Additionally it is necessary to register your LLM class in the enum `ModelLiterals` in order for it beeing selectable.

In case you like to add additional prompts, you create a new file in the corresponding folder under `src/main/resources/prompts`.
You can include placeholders which will be replaced in the prompt building process.
All currently available replacements are found in the Replacement enum.
After your prompt is finished you need to register it either in the `PromptGraphCode` or `PromptKeyword` enum.
There it is required to provide the files path and a list of replacements that are included in the prompt.

## Building
Using maven you can run `mvn clean install` to build the package.
After a successful build an executable jar file is found in the target folder.
