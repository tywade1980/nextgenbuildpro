#!/bin/bash

# Script to help transfer files from ai-full-stack-develo to ngbp repository
# This script should be run from the ngbp repository root directory after cloning

echo "=== NextGenBuildPro Repository File Transfer Script ==="
echo "This script helps transfer files from the wrong repository to the correct ngbp repository"
echo ""

# Check if we are in a git repository
if [ ! -d ".git" ]; then
    echo "Error: This script must be run from the root of the ngbp git repository"
    exit 1
fi

# Set source and destination paths
SOURCE_REPO="/home/runner/work/ai-full-stack-develo/ai-full-stack-develo"
DEST_REPO="."

echo "Source repository: $SOURCE_REPO"
echo "Destination repository: $DEST_REPO"
echo ""

# Check if source repository exists
if [ ! -d "$SOURCE_REPO" ]; then
    echo "Error: Source repository not found at $SOURCE_REPO"
    echo "Please update the SOURCE_REPO variable to point to the correct path"
    exit 1
fi

echo "Files to transfer:"
echo "=================="

# List of core files to transfer
CORE_FILES=(
    "package.json"
    "package-lock.json"
    "tsconfig.json"
    "vite.config.ts"
    "tailwind.config.js"
    "components.json"
    "theme.json"
    "runtime.config.json"
    "index.html"
    "LICENSE"
    ".gitignore"
    "PRD.md"
    "NAVIGATION_LIFECYCLE_COMPLETION.md"
    "SYSTEM_TEST_SUMMARY.md"
    "SECURITY.md"
    "validate-platform.js"
)

# List of directories to transfer completely
DIRECTORIES=(
    "src/components"
    "src/services"
    "src/utils"
    "src/templates"
    "src/hooks"
    "src/lib"
    "src/docs"
    "src/styles"
    ".github"
)

# Individual src files
SRC_FILES=(
    "src/App.tsx"
    "src/main.tsx"
    "src/index.css"
    "src/main.css"
    "src/prd.md"
    "src/vite-end.d.ts"
)

# Function to copy files safely
copy_file() {
    local src="$1"
    local dest="$2"
    
    if [ -f "$SOURCE_REPO/$src" ]; then
        # Create destination directory if it doesn't exist
        mkdir -p "$(dirname "$dest")"
        cp "$SOURCE_REPO/$src" "$dest"
        echo "✓ Copied: $src"
    else
        echo "✗ Missing: $src"
    fi
}

# Function to copy directories safely
copy_directory() {
    local src="$1"
    local dest="$2"
    
    if [ -d "$SOURCE_REPO/$src" ]; then
        # Create destination directory if it doesn't exist
        mkdir -p "$(dirname "$dest")"
        cp -r "$SOURCE_REPO/$src" "$dest"
        echo "✓ Copied directory: $src ($(find "$SOURCE_REPO/$src" -type f | wc -l) files)"
    else
        echo "✗ Missing directory: $src"
    fi
}

echo "1. Copying core configuration files..."
for file in "${CORE_FILES[@]}"; do
    copy_file "$file" "$file"
done

echo ""
echo "2. Copying individual src files..."
for file in "${SRC_FILES[@]}"; do
    copy_file "$file" "$file"
done

echo ""
echo "3. Copying complete directories..."
for dir in "${DIRECTORIES[@]}"; do
    copy_directory "$dir" "$dir"
done

echo ""
echo "=== Transfer Summary ==="

# Count transferred files
total_files=$(find . -name "*.tsx" -o -name "*.ts" -o -name "*.json" -o -name "*.md" -o -name "*.js" -o -name "*.css" -o -name "*.html" | grep -v node_modules | wc -l)

echo "Total files transferred: $total_files"
echo ""
echo "Next steps:"
echo "1. Review the transferred files"
echo "2. Update package.json name field to reflect ngbp"
echo "3. Update README.md for the ngbp repository"
echo "4. Run 'npm install' to install dependencies"
echo "5. Run 'npm run build' to test the build"
echo "6. Run 'npm run dev' to test the development server"
echo "7. Commit and push the changes to the ngbp repository"
echo "8. Create a pull request in the ngbp repository"
echo ""
echo "Important: After successful transfer and testing, the original repository"
echo "should be cleaned up to remove these files and reset to a basic template."