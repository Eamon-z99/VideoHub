#!/usr/bin/env node

const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

// 获取所有包含 package.json 的子目录
function getWorkspaces() {
  const workspaces = [];
  const frontendDir = __dirname;
  
  const entries = fs.readdirSync(frontendDir, { withFileTypes: true });
  
  for (const entry of entries) {
    if (entry.isDirectory() && entry.name !== 'node_modules') {
      const packageJsonPath = path.join(frontendDir, entry.name, 'package.json');
      if (fs.existsSync(packageJsonPath)) {
        const pkg = JSON.parse(fs.readFileSync(packageJsonPath, 'utf8'));
        if (pkg.scripts && pkg.scripts.preview) {
          workspaces.push({
            name: entry.name,
            path: entry.name
          });
        }
      }
    }
  }
  
  return workspaces;
}

// 生成 concurrently 命令
function generatePreviewCommand() {
  const workspaces = getWorkspaces();
  
  if (workspaces.length === 0) {
    console.error('未找到可预览的应用');
    process.exit(1);
  }
  
  const names = workspaces.map(w => w.name).join(',');
  const colors = ['cyan', 'magenta', 'green', 'yellow', 'blue', 'red', 'white'];
  const colorList = workspaces.map((_, i) => colors[i % colors.length]).join(',');
  
  const commands = workspaces.map(w => `npm run preview --workspace=${w.path}`).join(' ');
  const concurrentlyCmd = `concurrently -n "${names}" -c "${colorList}" ${commands}`;
  
  console.log('👀 预览应用:', workspaces.map(w => w.name).join(', '));
  console.log('');
  
  try {
    execSync(concurrentlyCmd, { stdio: 'inherit', cwd: __dirname });
  } catch (error) {
    process.exit(1);
  }
}

generatePreviewCommand();

