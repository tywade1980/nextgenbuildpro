# Package Updates - Known Issues and Workarounds

## Summary
The package.json has been updated with the following versions as requested:

### Dependencies
- firebase: 10.0.0 → 12.3.0
- react: 18.0.0 → 19.1.1
- react-native: 0.72.0 → 0.81.4
- uuid: 9.0.0 → 13.0.0

### Dev Dependencies
- @types/node: 20.19.17 → 24.6.1
- @types/react: 18.0.0 → 19.1.17
- @types/uuid: 9.0.0 → 10.0.0
- @typescript-eslint/eslint-plugin: 6.0.0 → 8.45.0
- @typescript-eslint/parser: 6.0.0 → 8.45.0
- eslint: 8.0.0 → 9.36.0
- jest: 29.7.0 → 30.2.0

## Known Issues

### UUID 13.0.0 and Jest Compatibility

**Issue**: UUID 13.0.0 is an ESM-only package, which can cause compatibility issues with Jest in CommonJS projects.

**Symptoms**:
```
SyntaxError: Unexpected token 'export'
```

**Workarounds**:

1. **Use UUID in production code only** (recommended for now):
   - UUID works fine in production Node.js and browser environments
   - For testing, mock the uuid module

2. **Mock UUID in tests**:
   ```javascript
   jest.mock('uuid', () => ({
     v4: jest.fn(() => 'test-uuid-1234')
   }));
   ```

3. **Use Jest with ESM support** (requires additional configuration):
   - Add `"type": "module"` to package.json
   - Update all imports to use .js extensions
   - Use `NODE_OPTIONS=--experimental-vm-modules npm test`

4. **Alternative**: Use uuid@10.0.1 if CommonJS compatibility is required:
   ```json
   "uuid": "^10.0.1"
   ```

### ESLint 9.36.0 Configuration

**Issue**: ESLint 9.x requires a new flat config format (eslint.config.js) instead of .eslintrc.js.

**Solution**: A new `eslint.config.js` file has been created with equivalent configuration. The old `.eslintrc.js` can be removed if desired.

## Verification

All packages install successfully:
```bash
npm install
```

TypeScript compilation works:
```bash
npm run build
```

ESLint runs successfully:
```bash
npm run lint
```

Validation script passes:
```bash
node validate.js
```

## Next Steps

1. Consider implementing UUID mocking in affected tests
2. Remove old `.eslintrc.js` file if no longer needed
3. Test the updated packages in your development environment
4. If UUID ESM issues persist, consider downgrading to uuid@10.0.1
