#!/usr/bin/env node

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
        workspaces.push({
          name: entry.name,
          path: path.join(frontendDir, entry.name)
        });
      }
    }
  }
  
  return workspaces;
}

// 删除目录
function removeDir(dirPath) {
  if (fs.existsSync(dirPath)) {
    try {
      fs.rmSync(dirPath, { recursive: true, force: true });
      return true;
    } catch (error) {
      console.error(`删除失败: ${dirPath}`, error.message);
      return false;
    }
  }
  return false;
}

// 清理所有工作区
function clean() {
  const workspaces = getWorkspaces();
  
  console.log('🧹 开始清理...\n');
  
  let cleaned = 0;
  
  for (const workspace of workspaces) {
    const nodeModulesPath = path.join(workspace.path, 'node_modules');
    const distPath = path.join(workspace.path, 'dist');
    
    if (removeDir(nodeModulesPath)) {
      console.log(`✅ 已清理 ${workspace.name}/node_modules`);
      cleaned++;
    }
    
    if (removeDir(distPath)) {
      console.log(`✅ 已清理 ${workspace.name}/dist`);
    }
  }
  
  // 清理根目录的 node_modules
  const rootNodeModules = path.join(__dirname, 'node_modules');
  if (removeDir(rootNodeModules)) {
    console.log(`✅ 已清理根目录 node_modules`);
  }
  
  console.log(`\n✨ 清理完成！共清理 ${cleaned} 个应用的依赖`);
}

clean();







