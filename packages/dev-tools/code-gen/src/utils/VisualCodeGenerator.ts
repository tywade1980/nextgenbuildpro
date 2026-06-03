import { CanvasComponent } from '@/components/VisualComponentBuilder'

export class VisualCodeGenerator {
  static generateReactCode(components: CanvasComponent[]): string {
    const imports = this.generateImports(components)
    const componentCode = this.generateComponentCode(components)
    
    return `${imports}

export default function GeneratedComponent() {
  return (
    <div className="relative w-full h-full">
${componentCode}
    </div>
  )
}`
  }

  private static generateImports(components: CanvasComponent[]): string {
    const imports = new Set(['React'])
    
    components.forEach(comp => {
      switch (comp.type) {
        case 'Button':
          imports.add('Button')
          break
        case 'Input':
          imports.add('Input')
          break
        case 'Textarea':
          imports.add('Textarea')
          break
        case 'Card':
          imports.add('Card')
          imports.add('CardContent')
          imports.add('CardDescription')
          imports.add('CardHeader')
          imports.add('CardTitle')
          break
      }
    })

    const uiImports = Array.from(imports).filter(imp => 
      ['Button', 'Input', 'Textarea', 'Card', 'CardContent', 'CardDescription', 'CardHeader', 'CardTitle'].includes(imp)
    )

    let importString = "import React from 'react'\n"
    
    if (uiImports.length > 0) {
      importString += `import { ${uiImports.join(', ')} } from '@/components/ui'\n`
    }

    return importString
  }

  private static generateComponentCode(components: CanvasComponent[], indent = 6): string {
    return components.map(comp => this.generateSingleComponent(comp, indent)).join('\n')
  }

  private static generateSingleComponent(component: CanvasComponent, indent = 6): string {
    const indentStr = ' '.repeat(indent)
    const style = this.generateInlineStyle(component)
    const className = component.props.className ? ` className="${component.props.className}"` : ''
    
    switch (component.type) {
      case 'Button':
        const variant = component.props.variant !== 'default' ? ` variant="${component.props.variant}"` : ''
        return `${indentStr}<Button${style}${className}${variant}>${component.props.children}</Button>`
      
      case 'Input':
        const placeholder = component.props.placeholder ? ` placeholder="${component.props.placeholder}"` : ''
        const inputType = component.props.type !== 'text' ? ` type="${component.props.type}"` : ''
        return `${indentStr}<Input${style}${className}${placeholder}${inputType} />`
      
      case 'Textarea':
        const textareaPlaceholder = component.props.placeholder ? ` placeholder="${component.props.placeholder}"` : ''
        const rows = component.props.rows ? ` rows={${component.props.rows}}` : ''
        return `${indentStr}<Textarea${style}${className}${textareaPlaceholder}${rows} />`
      
      case 'Card':
        const cardChildren = component.children.length > 0 
          ? `\n${this.generateComponentCode(component.children, indent + 2)}\n${indentStr}`
          : ''
        return `${indentStr}<Card${style}${className}>
${indentStr}  <CardHeader>
${indentStr}    <CardTitle>${component.props.title}</CardTitle>
${indentStr}    <CardDescription>${component.props.description}</CardDescription>
${indentStr}  </CardHeader>
${indentStr}  <CardContent>${cardChildren}
${indentStr}  </CardContent>
${indentStr}</Card>`
      
      case 'img':
        const src = component.props.src ? ` src="${component.props.src}"` : ''
        const alt = component.props.alt ? ` alt="${component.props.alt}"` : ''
        return `${indentStr}<img${style}${className}${src}${alt} />`
      
      case 'p':
        return `${indentStr}<p${style}${className}>${component.props.children}</p>`
      
      case 'div':
        const divChildren = component.children.length > 0 
          ? `\n${this.generateComponentCode(component.children, indent + 2)}\n${indentStr}`
          : ''
        return `${indentStr}<div${style}${className}>${divChildren}</div>`
      
      default:
        return `${indentStr}<div${style}${className}>Unknown component: ${component.type}</div>`
    }
  }

  private static generateInlineStyle(component: CanvasComponent): string {
    const style = {
      position: 'absolute' as const,
      left: `${component.position.x}px`,
      top: `${component.position.y}px`,
      width: `${component.size.width}px`,
      height: `${component.size.height}px`
    }

    const styleString = Object.entries(style)
      .map(([key, value]) => `${key}: '${value}'`)
      .join(', ')

    return ` style={{ ${styleString} }}`
  }

  static generateVueCode(components: CanvasComponent[]): string {
    const componentCode = this.generateVueComponentCode(components)
    
    return `<template>
  <div class="relative w-full h-full">
${componentCode}
  </div>
</template>

<script setup>
import { ref } from 'vue'
</script>

<style scoped>
/* Component styles */
</style>`
  }

  private static generateVueComponentCode(components: CanvasComponent[], indent = 4): string {
    return components.map(comp => this.generateSingleVueComponent(comp, indent)).join('\n')
  }

  private static generateSingleVueComponent(component: CanvasComponent, indent = 4): string {
    const indentStr = ' '.repeat(indent)
    const style = this.generateVueInlineStyle(component)
    const className = component.props.className ? ` class="${component.props.className}"` : ''
    
    switch (component.type) {
      case 'Button':
        return `${indentStr}<button${style}${className}>${component.props.children}</button>`
      
      case 'Input':
        const placeholder = component.props.placeholder ? ` placeholder="${component.props.placeholder}"` : ''
        const inputType = component.props.type !== 'text' ? ` type="${component.props.type}"` : ''
        return `${indentStr}<input${style}${className}${placeholder}${inputType} />`
      
      case 'Textarea':
        const textareaPlaceholder = component.props.placeholder ? ` placeholder="${component.props.placeholder}"` : ''
        const rows = component.props.rows ? ` rows="${component.props.rows}"` : ''
        return `${indentStr}<textarea${style}${className}${textareaPlaceholder}${rows}></textarea>`
      
      case 'Card':
        const cardChildren = component.children.length > 0 
          ? `\n${this.generateVueComponentCode(component.children, indent + 2)}\n${indentStr}`
          : ''
        return `${indentStr}<div${style}${className}>
${indentStr}  <div class="card-header">
${indentStr}    <h3>${component.props.title}</h3>
${indentStr}    <p>${component.props.description}</p>
${indentStr}  </div>
${indentStr}  <div class="card-content">${cardChildren}
${indentStr}  </div>
${indentStr}</div>`
      
      case 'img':
        const src = component.props.src ? ` src="${component.props.src}"` : ''
        const alt = component.props.alt ? ` alt="${component.props.alt}"` : ''
        return `${indentStr}<img${style}${className}${src}${alt} />`
      
      case 'p':
        return `${indentStr}<p${style}${className}>${component.props.children}</p>`
      
      case 'div':
        const divChildren = component.children.length > 0 
          ? `\n${this.generateVueComponentCode(component.children, indent + 2)}\n${indentStr}`
          : ''
        return `${indentStr}<div${style}${className}>${divChildren}</div>`
      
      default:
        return `${indentStr}<div${style}${className}>Unknown component: ${component.type}</div>`
    }
  }

  private static generateVueInlineStyle(component: CanvasComponent): string {
    const style = `position: absolute; left: ${component.position.x}px; top: ${component.position.y}px; width: ${component.size.width}px; height: ${component.size.height}px;`
    return ` style="${style}"`
  }

  static generateAngularCode(components: CanvasComponent[]): string {
    const componentCode = this.generateAngularComponentCode(components)
    
    return `import { Component } from '@angular/core';

@Component({
  selector: 'app-generated',
  template: \`
    <div class="relative w-full h-full">
${componentCode}
    </div>
  \`,
  styleUrls: ['./generated.component.css']
})
export class GeneratedComponent {
  // Component logic here
}`
  }

  private static generateAngularComponentCode(components: CanvasComponent[], indent = 6): string {
    return components.map(comp => this.generateSingleAngularComponent(comp, indent)).join('\n')
  }

  private static generateSingleAngularComponent(component: CanvasComponent, indent = 6): string {
    const indentStr = ' '.repeat(indent)
    const style = this.generateAngularInlineStyle(component)
    const className = component.props.className ? ` class="${component.props.className}"` : ''
    
    switch (component.type) {
      case 'Button':
        return `${indentStr}<button${style}${className}>${component.props.children}</button>`
      
      case 'Input':
        const placeholder = component.props.placeholder ? ` placeholder="${component.props.placeholder}"` : ''
        const inputType = component.props.type !== 'text' ? ` type="${component.props.type}"` : ''
        return `${indentStr}<input${style}${className}${placeholder}${inputType} />`
      
      case 'Textarea':
        const textareaPlaceholder = component.props.placeholder ? ` placeholder="${component.props.placeholder}"` : ''
        const rows = component.props.rows ? ` rows="${component.props.rows}"` : ''
        return `${indentStr}<textarea${style}${className}${textareaPlaceholder}${rows}></textarea>`
      
      case 'Card':
        const cardChildren = component.children.length > 0 
          ? `\n${this.generateAngularComponentCode(component.children, indent + 2)}\n${indentStr}`
          : ''
        return `${indentStr}<div${style}${className}>
${indentStr}  <div class="card-header">
${indentStr}    <h3>${component.props.title}</h3>
${indentStr}    <p>${component.props.description}</p>
${indentStr}  </div>
${indentStr}  <div class="card-content">${cardChildren}
${indentStr}  </div>
${indentStr}</div>`
      
      case 'img':
        const src = component.props.src ? ` src="${component.props.src}"` : ''
        const alt = component.props.alt ? ` alt="${component.props.alt}"` : ''
        return `${indentStr}<img${style}${className}${src}${alt} />`
      
      case 'p':
        return `${indentStr}<p${style}${className}>${component.props.children}</p>`
      
      case 'div':
        const divChildren = component.children.length > 0 
          ? `\n${this.generateAngularComponentCode(component.children, indent + 2)}\n${indentStr}`
          : ''
        return `${indentStr}<div${style}${className}>${divChildren}</div>`
      
      default:
        return `${indentStr}<div${style}${className}>Unknown component: ${component.type}</div>`
    }
  }

  private static generateAngularInlineStyle(component: CanvasComponent): string {
    const style = `position: absolute; left: ${component.position.x}px; top: ${component.position.y}px; width: ${component.size.width}px; height: ${component.size.height}px;`
    return ` style="${style}"`
  }

  static generateCSS(components: CanvasComponent[]): string {
    let css = `/* Generated CSS */
.relative {
  position: relative;
}

.w-full {
  width: 100%;
}

.h-full {
  height: 100%;
}

/* Component styles */
`

    const uniqueClasses = new Set<string>()
    this.extractClasses(components, uniqueClasses)

    uniqueClasses.forEach(className => {
      if (className && !className.includes('w-') && !className.includes('h-') && !className.includes('relative')) {
        css += `.${className} {
  /* Add your styles here */
}

`
      }
    })

    return css
  }

  private static extractClasses(components: CanvasComponent[], classSet: Set<string>) {
    components.forEach(comp => {
      if (comp.props.className) {
        comp.props.className.split(' ').forEach(cls => {
          if (cls.trim()) classSet.add(cls.trim())
        })
      }
      if (comp.children) {
        this.extractClasses(comp.children, classSet)
      }
    })
  }

  static generateJSON(components: CanvasComponent[]): string {
    return JSON.stringify(components, null, 2)
  }
}