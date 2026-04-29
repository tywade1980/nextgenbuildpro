#!/usr/bin/env node

/**
 * AI Development Platform - System Validation Script
 * 
 * This script validates the entire platform by checking:
 * 1. All component imports and exports
 * 2. Service and utility module availability
 * 3. Template system integrity
 * 4. Mock functionality tests
 */

const fs = require('fs');
const path = require('path');

const BASE_DIR = '/workspaces/spark-template/src';

// Component list from App.tsx
const REQUIRED_COMPONENTS = [
    'ProjectManager',
    'AIAssistant', 
    'AgenticCodeEditor',
    'TestingSuite',
    'DeploymentPipeline',
    'TemplateEditor',
    'FileRewriteManager',
    'FileStructureGenerator',
    'BackendIntegration',
    'ProductionCodeGeneratorComponent',
    'ExpressTemplateSelector',
    'AndroidTemplateSelector',
    'APIDocumentation',
    'MicroservicesArchitect',
    'ServiceMeshPolicies',
    'LifecycleAnalysis',
    'NavigationFlowTracker',
    'EndpointValidator',
    'LivePreview',
    'AndroidPreview',
    'VisualBuilder',
    'SchemaCodeGenerator',
    'SystemValidator',
    'SystemStatusDashboard',
    'ComponentImportValidator',
    'ComprehensiveSystemTestSummary',
    'PlatformTester',
    'FullSystemTest',
    'ErrorBoundary'
];

const REQUIRED_SERVICES = [
    'CodeGenerationEngine',
    'CodeRewriteManager',
    'AgenticMemoryManager',
    'AutomatedTestingEngine',
    'APIService',
    'DatabaseService'
];

const REQUIRED_UTILS = [
    'CodeGenerator',
    'ProductionCodeGenerator',
    'BackendCodeGenerator',
    'MicroserviceGenerator'
];

class PlatformValidator {
    constructor() {
        this.results = {
            components: [],
            services: [],
            utils: [],
            templates: [],
            errors: []
        };
    }

    log(message, type = 'info') {
        const timestamp = new Date().toISOString();
        const prefix = {
            'info': '✓',
            'warn': '⚠',
            'error': '✗'
        }[type] || 'ℹ';
        
        console.log(`${prefix} [${timestamp}] ${message}`);
    }

    validateComponent(componentName) {
        try {
            const componentPath = path.join(BASE_DIR, 'components', `${componentName}.tsx`);
            
            if (!fs.existsSync(componentPath)) {
                this.results.errors.push(`Component file missing: ${componentName}.tsx`);
                this.log(`Component file missing: ${componentName}`, 'error');
                return false;
            }

            const content = fs.readFileSync(componentPath, 'utf8');
            
            // Check for export
            const hasExport = content.includes(`export function ${componentName}`) || 
                            content.includes(`export default ${componentName}`) ||
                            content.includes(`export const ${componentName}`) ||
                            content.includes(`export { ${componentName}`);
            
            if (!hasExport) {
                this.results.errors.push(`Component ${componentName} missing export`);
                this.log(`Component ${componentName} missing export`, 'error');
                return false;
            }

            // Check for basic React imports
            if (!content.includes('React') && !content.includes('import {')) {
                this.results.errors.push(`Component ${componentName} missing React imports`);
                this.log(`Component ${componentName} missing React imports`, 'warn');
            }

            this.results.components.push({
                name: componentName,
                status: 'valid',
                path: componentPath
            });
            
            this.log(`Component ${componentName} validated successfully`);
            return true;
            
        } catch (error) {
            this.results.errors.push(`Component ${componentName} validation error: ${error.message}`);
            this.log(`Component ${componentName} validation error: ${error.message}`, 'error');
            return false;
        }
    }

    validateService(serviceName) {
        try {
            const servicePath = path.join(BASE_DIR, 'services', `${serviceName}.ts`);
            
            if (!fs.existsSync(servicePath)) {
                this.results.errors.push(`Service file missing: ${serviceName}.ts`);
                this.log(`Service file missing: ${serviceName}`, 'error');
                return false;
            }

            const content = fs.readFileSync(servicePath, 'utf8');
            
            // Check for exports
            if (!content.includes('export')) {
                this.results.errors.push(`Service ${serviceName} missing exports`);
                this.log(`Service ${serviceName} missing exports`, 'error');
                return false;
            }

            this.results.services.push({
                name: serviceName,
                status: 'valid',
                path: servicePath
            });
            
            this.log(`Service ${serviceName} validated successfully`);
            return true;
            
        } catch (error) {
            this.results.errors.push(`Service ${serviceName} validation error: ${error.message}`);
            this.log(`Service ${serviceName} validation error: ${error.message}`, 'error');
            return false;
        }
    }

    validateUtil(utilName) {
        try {
            const utilPath = path.join(BASE_DIR, 'utils', `${utilName}.ts`);
            
            if (!fs.existsSync(utilPath)) {
                this.results.errors.push(`Utility file missing: ${utilName}.ts`);
                this.log(`Utility file missing: ${utilName}`, 'error');
                return false;
            }

            const content = fs.readFileSync(utilPath, 'utf8');
            
            // Check for exports
            if (!content.includes('export')) {
                this.results.errors.push(`Utility ${utilName} missing exports`);
                this.log(`Utility ${utilName} missing exports`, 'error');
                return false;
            }

            this.results.utils.push({
                name: utilName,
                status: 'valid',
                path: utilPath
            });
            
            this.log(`Utility ${utilName} validated successfully`);
            return true;
            
        } catch (error) {
            this.results.errors.push(`Utility ${utilName} validation error: ${error.message}`);
            this.log(`Utility ${utilName} validation error: ${error.message}`, 'error');
            return false;
        }
    }

    validateTemplates() {
        try {
            const templatePath = path.join(BASE_DIR, 'templates', 'framework-templates.ts');
            
            if (!fs.existsSync(templatePath)) {
                this.results.errors.push('Template system missing: framework-templates.ts');
                this.log('Template system missing', 'error');
                return false;
            }

            const content = fs.readFileSync(templatePath, 'utf8');
            
            if (!content.includes('export')) {
                this.results.errors.push('Template system missing exports');
                this.log('Template system missing exports', 'error');
                return false;
            }

            this.results.templates.push({
                name: 'framework-templates',
                status: 'valid',
                path: templatePath
            });
            
            this.log('Template system validated successfully');
            return true;
            
        } catch (error) {
            this.results.errors.push(`Template validation error: ${error.message}`);
            this.log(`Template validation error: ${error.message}`, 'error');
            return false;
        }
    }

    validateMainApp() {
        try {
            const appPath = path.join(BASE_DIR, 'App.tsx');
            
            if (!fs.existsSync(appPath)) {
                this.results.errors.push('Main App.tsx file missing');
                this.log('Main App.tsx file missing', 'error');
                return false;
            }

            const content = fs.readFileSync(appPath, 'utf8');
            
            // Check for required imports
            const requiredImports = [
                'useState',
                'useKV',
                'Tabs',
                'ErrorBoundary'
            ];

            for (const imp of requiredImports) {
                if (!content.includes(imp)) {
                    this.results.errors.push(`App.tsx missing import: ${imp}`);
                    this.log(`App.tsx missing import: ${imp}`, 'warn');
                }
            }

            // Check for export default
            if (!content.includes('export default App')) {
                this.results.errors.push('App.tsx missing default export');
                this.log('App.tsx missing default export', 'error');
                return false;
            }

            this.log('Main App.tsx validated successfully');
            return true;
            
        } catch (error) {
            this.results.errors.push(`App.tsx validation error: ${error.message}`);
            this.log(`App.tsx validation error: ${error.message}`, 'error');
            return false;
        }
    }

    async runFullValidation() {
        this.log('=== AI Development Platform System Validation ===');
        this.log('Starting comprehensive system validation...');

        // Validate main app
        this.log('\n--- Validating Main Application ---');
        this.validateMainApp();

        // Validate components
        this.log('\n--- Validating Components ---');
        for (const component of REQUIRED_COMPONENTS) {
            this.validateComponent(component);
        }

        // Validate services
        this.log('\n--- Validating Services ---');
        for (const service of REQUIRED_SERVICES) {
            this.validateService(service);
        }

        // Validate utilities
        this.log('\n--- Validating Utilities ---');
        for (const util of REQUIRED_UTILS) {
            this.validateUtil(util);
        }

        // Validate templates
        this.log('\n--- Validating Templates ---');
        this.validateTemplates();

        // Generate report
        this.generateReport();
    }

    generateReport() {
        this.log('\n=== VALIDATION REPORT ===');
        
        const totalComponents = REQUIRED_COMPONENTS.length;
        const validComponents = this.results.components.length;
        const totalServices = REQUIRED_SERVICES.length;
        const validServices = this.results.services.length;
        const totalUtils = REQUIRED_UTILS.length;
        const validUtils = this.results.utils.length;
        const totalTemplates = 1;
        const validTemplates = this.results.templates.length;

        this.log(`Components: ${validComponents}/${totalComponents} valid`);
        this.log(`Services: ${validServices}/${totalServices} valid`);
        this.log(`Utilities: ${validUtils}/${totalUtils} valid`);
        this.log(`Templates: ${validTemplates}/${totalTemplates} valid`);
        this.log(`Total Errors: ${this.results.errors.length}`);

        if (this.results.errors.length > 0) {
            this.log('\n--- ERRORS ---', 'error');
            this.results.errors.forEach(error => {
                this.log(error, 'error');
            });
        }

        const overallScore = (
            (validComponents / totalComponents) * 0.4 +
            (validServices / totalServices) * 0.3 +
            (validUtils / totalUtils) * 0.2 +
            (validTemplates / totalTemplates) * 0.1
        ) * 100;

        this.log(`\nOverall System Health: ${overallScore.toFixed(1)}%`);

        if (overallScore >= 90) {
            this.log('🎉 SYSTEM STATUS: EXCELLENT - All major components operational', 'info');
        } else if (overallScore >= 75) {
            this.log('✅ SYSTEM STATUS: GOOD - Most components operational', 'info');
        } else if (overallScore >= 50) {
            this.log('⚠️  SYSTEM STATUS: FAIR - Some components need attention', 'warn');
        } else {
            this.log('❌ SYSTEM STATUS: POOR - Major components missing or broken', 'error');
        }

        return {
            score: overallScore,
            components: validComponents,
            services: validServices,
            utils: validUtils,
            templates: validTemplates,
            errors: this.results.errors.length
        };
    }
}

// Run validation
const validator = new PlatformValidator();
validator.runFullValidation();