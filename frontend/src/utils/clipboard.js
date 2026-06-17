import ClipboardJS from 'clipboard'

export function copyToClipboard(text, triggerElement) {
  return new Promise((resolve, reject) => {
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text)
        .then(() => resolve(true))
        .catch(() => fallbackCopy(text, resolve, reject))
    } else {
      fallbackCopy(text, resolve, reject)
    }
  })
}

function fallbackCopy(text, resolve, reject) {
  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.style.position = 'fixed'
  textarea.style.opacity = '0'
  document.body.appendChild(textarea)
  textarea.select()
  try {
    document.execCommand('copy')
    resolve(true)
  } catch (err) {
    reject(err)
  } finally {
    document.body.removeChild(textarea)
  }
}

export function initClipboard(selector, onSuccess, onError) {
  const clipboard = new ClipboardJS(selector)
  clipboard.on('success', (e) => {
    e.clearSelection()
    if (onSuccess) onSuccess(e)
  })
  clipboard.on('error', (e) => {
    if (onError) onError(e)
  })
  return clipboard
}

export default ClipboardJS
