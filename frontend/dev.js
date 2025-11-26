#!/usr/bin/env node

const { spawn } = require('child_process');
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
        if (pkg.scripts && pkg.scripts.dev) {
          workspaces.push({
            name: entry.name,
            path: path.join(frontendDir, entry.name)
          });
        }
      }
    }
  }
  
  return workspaces;
}

// 启动所有应用
function startApps() {
  const workspaces = getWorkspaces();
  
  if (workspaces.length === 0) {
    console.error('未找到可启动的应用');
    process.exit(1);
  }
  
  console.log('🚀 启动应用:', workspaces.map(w => w.name).join(', '));
  console.log('');
  
  const colors = ['cyan', 'magenta', 'green', 'yellow', 'blue', 'red', 'white'];
  const processes = [];
  
  // 为每个应用启动进程
  workspaces.forEach((workspace, index) => {
    const color = colors[index % colors.length];
    const workspacePath = workspace.path;
    
    // 直接使用 npm，设置工作目录
    const proc = spawn('npm', ['run', 'dev'], {
      cwd: workspacePath,
      stdio: 'inherit',
      shell: true,
      env: process.env
    });
    
    processes.push(proc);
    
    proc.on('error', (error) => {
      console.error(`[${workspace.name}] 启动失败:`, error.message);
    });
    
    proc.on('exit', (code) => {
      if (code !== 0 && code !== null) {
        console.error(`[${workspace.name}] 退出，代码: ${code}`);
      }
    });
  });
  
  // 处理退出信号
  process.on('SIGINT', () => {
    console.log('\n正在停止所有应用...');
    processes.forEach(proc => {
      proc.kill('SIGINT');
    });
    process.exit(0);
  });
  
  process.on('SIGTERM', () => {
    processes.forEach(proc => {
      proc.kill('SIGTERM');
    });
    process.exit(0);
  });
}

startApps();
