import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'
import DOMPurify from 'dompurify'

marked.setOptions({
  highlight: function (code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(code, { language: lang }).value
      } catch (e) {
        // ignore
      }
    }
    return hljs.highlightAuto(code).value
  },
  breaks: true,
  gfm: true
})

export function renderMarkdown(content) {
  if (!content) return ''
  const html = marked(content)
  return DOMPurify.sanitize(html)
}

export function sanitizeHtml(html) {
  if (!html) return ''
  return DOMPurify.sanitize(html)
}

export function extractCodeBlocks(content) {
  if (!content) return []
  const regex = /```(\w+)?\n([\s\S]*?)```/g
  const blocks = []
  let match
  while ((match = regex.exec(content)) !== null) {
    blocks.push({
      language: match[1] || 'text',
      code: match[2].trim()
    })
  }
  return blocks
}

export { hljs, marked, DOMPurify }
