import { ref } from 'vue'

export function useFetch() {
  const reviewContent = ref('')

  async function askQuestion(question: string, botName: string) {
    const response = await fetch('/api/review/qa/ask', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        question: question,
        bot_name: botName
      })
    })

    const reader = response.body?.getReader()
    const decoder = new TextDecoder()

    while (true) {
      const { done, value } = await reader!.read()
      if (done) break

      const chunk = decoder.decode(value)
      const lines = chunk.split('\n')

      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const data = line.slice(6)
          if (data === '[DONE]') {
            console.log('回答结束')
            return
          }

          try {
            const json = JSON.parse(data)
            const content = json.choices[0].delta.content
            if (content) {
              console.log('接收到内容:', content)
              // 这里可以追加到 UI
              reviewContent.value += content
            }
          } catch (e) {
            // 解析错误，跳过
          }
        }
      }
    }
  }
  return { reviewContent, askQuestion }
}
