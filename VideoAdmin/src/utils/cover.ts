/** 与创作者端一致：相对路径封面走 /local-videos */
export function normalizeCover(url: string): string {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/local-videos/')) return url
  if (url.startsWith('/')) {
    if (url.startsWith('/uploads/') || url.startsWith('/uploads\\')) {
      return '/local-videos' + url
    }
    return url
  }
  if (url.startsWith('uploads/') || url.startsWith('uploads\\')) {
    return '/local-videos/' + url.replaceAll('\\', '/')
  }
  return '/local-videos/' + url.replaceAll('\\', '/')
}

export function rawCover(row: Record<string, unknown>): string {
  const v = row.cover_url ?? row.coverUrl
  return typeof v === 'string' ? v : ''
}

export function coverStyle(row: Record<string, unknown>): Record<string, string> {
  const u = normalizeCover(rawCover(row))
  if (!u) return {}
  return { backgroundImage: `url("${u.replace(/"/g, '\\"')}")` }
}

/** 与封面同规则，兼容后端返回 play_url/source_file/storage_path */
export function normalizeMediaUrl(url: string): string {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/local-videos/')) return url
  if (url.startsWith('/')) {
    if (url.startsWith('/uploads/') || url.startsWith('/uploads\\')) {
      return '/local-videos' + url
    }
    return url
  }
  if (url.startsWith('uploads/') || url.startsWith('uploads\\')) {
    return '/local-videos/' + url.replaceAll('\\', '/')
  }
  return '/local-videos/' + url.replaceAll('\\', '/')
}

export function rawPlayUrl(row: Record<string, unknown>): string {
  const v = row.play_url ?? row.playUrl ?? row.source_file ?? row.sourceFile ?? row.storage_path ?? row.storagePath
  return typeof v === 'string' ? v : ''
}
