import * as vscode from 'vscode';
import { HuggingFaceModel, CodeSnippet } from '../types';

export class CodeSnippetProvider implements vscode.CompletionItemProvider {
    
    async insertCodeSnippet(model: HuggingFaceModel): Promise<void> {
        const editor = vscode.window.activeTextEditor;
        if (!editor) {
            vscode.window.showErrorMessage('No active editor found');
            return;
        }

        const snippets = this.getCodeSnippetsForModel(model);
        const selectedSnippet = await vscode.window.showQuickPick(
            snippets.map(s => ({
                label: s.description,
                description: s.language,
                detail: s.task,
                snippet: s
            })),
            { 
                placeHolder: `Select a code snippet for ${model.id}:`,
                matchOnDescription: true,
                matchOnDetail: true
            }
        );

        if (selectedSnippet) {
            const position = editor.selection.active;
            await editor.edit(editBuilder => {
                editBuilder.insert(position, selectedSnippet.snippet.code);
            });
        }
    }

    provideCompletionItems(
        document: vscode.TextDocument,
        position: vscode.Position,
        token: vscode.CancellationToken,
        context: vscode.CompletionContext
    ): vscode.ProviderResult<vscode.CompletionItem[] | vscode.CompletionList> {
        
        const linePrefix = document.lineAt(position).text.substr(0, position.character);
        
        // Trigger on specific patterns
        if (!linePrefix.includes('from transformers') && 
            !linePrefix.includes('pipeline') && 
            !linePrefix.includes('AutoModel') &&
            !linePrefix.includes('load_dataset')) {
            return undefined;
        }

        const completions: vscode.CompletionItem[] = [];

        // Add common Hugging Face patterns
        const commonSnippets = this.getCommonSnippets();
        
        for (const snippet of commonSnippets) {
            const completion = new vscode.CompletionItem(snippet.description, vscode.CompletionItemKind.Snippet);
            completion.insertText = new vscode.SnippetString(snippet.code);
            completion.documentation = new vscode.MarkdownString(snippet.description);
            completion.detail = `Hugging Face - ${snippet.language}`;
            completions.push(completion);
        }

        return completions;
    }

    private getCodeSnippetsForModel(model: HuggingFaceModel): CodeSnippet[] {
        const snippets: CodeSnippet[] = [];
        const modelId = model.id;
        const task = model.pipeline_tag;

        // Task-specific snippets
        switch (task) {
            case 'text-generation':
                snippets.push(...this.getTextGenerationSnippets(modelId));
                break;
            case 'text-classification':
                snippets.push(...this.getTextClassificationSnippets(modelId));
                break;
            case 'token-classification':
                snippets.push(...this.getTokenClassificationSnippets(modelId));
                break;
            case 'question-answering':
                snippets.push(...this.getQuestionAnsweringSnippets(modelId));
                break;
            case 'fill-mask':
                snippets.push(...this.getFillMaskSnippets(modelId));
                break;
            case 'summarization':
                snippets.push(...this.getSummarizationSnippets(modelId));
                break;
            case 'translation':
                snippets.push(...this.getTranslationSnippets(modelId));
                break;
            case 'image-classification':
                snippets.push(...this.getImageClassificationSnippets(modelId));
                break;
            case 'automatic-speech-recognition':
                snippets.push(...this.getASRSnippets(modelId));
                break;
            default:
                snippets.push(...this.getGenericSnippets(modelId, task));
        }

        // Add general purpose snippets
        snippets.push(...this.getAdvancedSnippets(modelId));

        return snippets;
    }

    private getTextGenerationSnippets(modelId: string): CodeSnippet[] {
        return [
            {
                language: 'Python',
                description: 'Basic text generation',
                task: 'text-generation',
                code: `from transformers import pipeline

# Initialize the text generation pipeline
generator = pipeline('text-generation', model='${modelId}')

# Generate text
prompt = "Once upon a time"
result = generator(prompt, max_length=100, num_return_sequences=1, temperature=0.7)
print(result[0]['generated_text'])
`
            },
            {
                language: 'Python',
                description: 'Text generation with custom parameters',
                task: 'text-generation',
                code: `from transformers import pipeline

# Initialize with custom parameters
generator = pipeline(
    'text-generation', 
    model='${modelId}',
    device=0 if torch.cuda.is_available() else -1  # Use GPU if available
)

# Generate with advanced parameters
prompt = "The future of AI is"
outputs = generator(
    prompt,
    max_length=150,
    min_length=50,
    num_return_sequences=3,
    temperature=0.8,
    top_p=0.9,
    do_sample=True,
    pad_token_id=generator.tokenizer.eos_token_id
)

for i, output in enumerate(outputs):
    print(f"Generation {i+1}: {output['generated_text']}")
`
            }
        ];
    }

    private getTextClassificationSnippets(modelId: string): CodeSnippet[] {
        return [
            {
                language: 'Python',
                description: 'Text classification',
                task: 'text-classification',
                code: `from transformers import pipeline

# Initialize the classifier
classifier = pipeline('text-classification', model='${modelId}')

# Classify single text
text = "I love this product!"
result = classifier(text)
print(f"Label: {result[0]['label']}, Score: {result[0]['score']:.4f}")

# Classify multiple texts
texts = ["Great product!", "Not satisfied", "Could be better"]
results = classifier(texts)
for text, result in zip(texts, results):
    print(f"'{text}' -> {result[0]['label']} ({result[0]['score']:.4f})")
`
            }
        ];
    }

    private getTokenClassificationSnippets(modelId: string): CodeSnippet[] {
        return [
            {
                language: 'Python',
                description: 'Named Entity Recognition',
                task: 'token-classification',
                code: `from transformers import pipeline

# Initialize NER pipeline
ner = pipeline('ner', model='${modelId}', aggregation_strategy='simple')

# Extract entities
text = "Apple Inc. was founded by Steve Jobs in Cupertino, California."
entities = ner(text)

print("Named Entities:")
for entity in entities:
    print(f"  {entity['word']}: {entity['entity_group']} (confidence: {entity['score']:.4f})")
`
            }
        ];
    }

    private getQuestionAnsweringSnippets(modelId: string): CodeSnippet[] {
        return [
            {
                language: 'Python',
                description: 'Question Answering',
                task: 'question-answering',
                code: `from transformers import pipeline

# Initialize QA pipeline
qa_pipeline = pipeline('question-answering', model='${modelId}')

# Define context and question
context = """
The Amazon rainforest is a moist broadleaf forest that covers most of the Amazon basin 
of South America. This basin encompasses 7,000,000 square kilometers, of which 
5,500,000 square kilometers are covered by the rainforest.
"""

question = "How large is the Amazon rainforest?"

# Get answer
result = qa_pipeline(question=question, context=context)
print(f"Answer: {result['answer']}")
print(f"Confidence: {result['score']:.4f}")
print(f"Start: {result['start']}, End: {result['end']}")
`
            }
        ];
    }

    private getFillMaskSnippets(modelId: string): CodeSnippet[] {
        return [
            {
                language: 'Python',
                description: 'Fill Mask',
                task: 'fill-mask',
                code: `from transformers import pipeline

# Initialize fill-mask pipeline
fill_mask = pipeline('fill-mask', model='${modelId}')

# Fill in the mask
text = "The capital of France is [MASK]."
result = fill_mask(text)

print("Predictions:")
for prediction in result:
    print(f"  {prediction['token_str']}: {prediction['score']:.4f}")
`
            }
        ];
    }

    private getSummarizationSnippets(modelId: string): CodeSnippet[] {
        return [
            {
                language: 'Python',
                description: 'Text Summarization',
                task: 'summarization',
                code: `from transformers import pipeline

# Initialize summarization pipeline
summarizer = pipeline('summarization', model='${modelId}')

# Text to summarize
article = """
Your long article text goes here. This could be a news article, 
research paper, or any long-form content that you want to summarize.
Make sure the text is long enough to benefit from summarization.
"""

# Generate summary
summary = summarizer(article, max_length=100, min_length=30, do_sample=False)
print("Summary:")
print(summary[0]['summary_text'])
`
            }
        ];
    }

    private getTranslationSnippets(modelId: string): CodeSnippet[] {
        return [
            {
                language: 'Python',
                description: 'Translation',
                task: 'translation',
                code: `from transformers import pipeline

# Initialize translation pipeline
translator = pipeline('translation', model='${modelId}')

# Translate text
text = "Hello, how are you today?"
result = translator(text)
print(f"Original: {text}")
print(f"Translation: {result[0]['translation_text']}")

# Translate multiple texts
texts = ["Good morning", "Thank you", "Goodbye"]
results = translator(texts)
for original, translated in zip(texts, results):
    print(f"'{original}' -> '{translated['translation_text']}'")
`
            }
        ];
    }

    private getImageClassificationSnippets(modelId: string): CodeSnippet[] {
        return [
            {
                language: 'Python',
                description: 'Image Classification',
                task: 'image-classification',
                code: `from transformers import pipeline
from PIL import Image

# Initialize image classification pipeline
classifier = pipeline('image-classification', model='${modelId}')

# Load and classify image
image = Image.open('path/to/your/image.jpg')
result = classifier(image)

print("Classification results:")
for prediction in result:
    print(f"  {prediction['label']}: {prediction['score']:.4f}")
`
            }
        ];
    }

    private getASRSnippets(modelId: string): CodeSnippet[] {
        return [
            {
                language: 'Python',
                description: 'Automatic Speech Recognition',
                task: 'automatic-speech-recognition',
                code: `from transformers import pipeline

# Initialize ASR pipeline
asr = pipeline('automatic-speech-recognition', model='${modelId}')

# Transcribe audio file
audio_file = 'path/to/your/audio.wav'
result = asr(audio_file)
print(f"Transcription: {result['text']}")

# You can also use with microphone input or other audio sources
`
            }
        ];
    }

    private getGenericSnippets(modelId: string, task?: string): CodeSnippet[] {
        return [
            {
                language: 'Python',
                description: 'Generic model usage',
                task: task || 'generic',
                code: `from transformers import pipeline

# Initialize pipeline
pipe = pipeline('${task || 'feature-extraction'}', model='${modelId}')

# Use the model
input_data = "Your input here"
result = pipe(input_data)
print(result)
`
            }
        ];
    }

    private getAdvancedSnippets(modelId: string): CodeSnippet[] {
        return [
            {
                language: 'Python',
                description: 'Advanced usage with AutoModel and AutoTokenizer',
                task: 'advanced',
                code: `from transformers import AutoTokenizer, AutoModel
import torch

# Load tokenizer and model
tokenizer = AutoTokenizer.from_pretrained('${modelId}')
model = AutoModel.from_pretrained('${modelId}')

# Tokenize input
text = "Your input text here"
inputs = tokenizer(text, return_tensors="pt", padding=True, truncation=True)

# Get model outputs
with torch.no_grad():
    outputs = model(**inputs)

# Access different outputs
last_hidden_states = outputs.last_hidden_state
print(f"Hidden states shape: {last_hidden_states.shape}")

# If you need pooled output for classification tasks
if hasattr(outputs, 'pooler_output'):
    pooled_output = outputs.pooler_output
    print(f"Pooled output shape: {pooled_output.shape}")
`
            },
            {
                language: 'Python',
                description: 'Batch processing with custom dataset',
                task: 'batch',
                code: `from transformers import AutoTokenizer, AutoModel
from torch.utils.data import DataLoader
import torch

# Initialize model and tokenizer
tokenizer = AutoTokenizer.from_pretrained('${modelId}')
model = AutoModel.from_pretrained('${modelId}')

# Your dataset
texts = ["Text 1", "Text 2", "Text 3", "..."]

# Tokenize all texts
encoded = tokenizer(texts, padding=True, truncation=True, return_tensors="pt")

# Create DataLoader for batch processing
dataset = torch.utils.data.TensorDataset(
    encoded['input_ids'], 
    encoded['attention_mask']
)
dataloader = DataLoader(dataset, batch_size=8)

# Process in batches
model.eval()
all_outputs = []

with torch.no_grad():
    for batch in dataloader:
        input_ids, attention_mask = batch
        outputs = model(input_ids=input_ids, attention_mask=attention_mask)
        all_outputs.append(outputs.last_hidden_state)

print(f"Processed {len(texts)} texts in batches")
`
            }
        ];
    }

    private getCommonSnippets(): CodeSnippet[] {
        return [
            {
                language: 'Python',
                description: 'Import transformers pipeline',
                task: 'import',
                code: 'from transformers import pipeline'
            },
            {
                language: 'Python',
                description: 'Import AutoModel and AutoTokenizer',
                task: 'import',
                code: `from transformers import AutoTokenizer, AutoModel
import torch`
            },
            {
                language: 'Python',
                description: 'Load dataset from Hugging Face',
                task: 'dataset',
                code: `from datasets import load_dataset

# Load dataset
dataset = load_dataset('dataset_name')
print(dataset)`
            },
            {
                language: 'Python',
                description: 'Basic model training setup',
                task: 'training',
                code: `from transformers import AutoTokenizer, AutoModel, Trainer, TrainingArguments
from datasets import load_dataset

# Load model and tokenizer
model_name = "model_name"
tokenizer = AutoTokenizer.from_pretrained(model_name)
model = AutoModel.from_pretrained(model_name)

# Load and preprocess dataset
dataset = load_dataset("your_dataset")

def tokenize_function(examples):
    return tokenizer(examples["text"], truncation=True, padding=True)

tokenized_dataset = dataset.map(tokenize_function, batched=True)

# Training arguments
training_args = TrainingArguments(
    output_dir="./results",
    num_train_epochs=3,
    per_device_train_batch_size=16,
    per_device_eval_batch_size=64,
    warmup_steps=500,
    weight_decay=0.01,
    logging_dir="./logs",
)

# Initialize trainer
trainer = Trainer(
    model=model,
    args=training_args,
    train_dataset=tokenized_dataset["train"],
    eval_dataset=tokenized_dataset["validation"],
)

# Start training
trainer.train()`
            }
        ];
    }
}