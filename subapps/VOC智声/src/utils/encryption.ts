import CryptoJS from 'crypto-js'

/**
 * AES 加密
 * @param value
 */
export const enCrypt = (value: string) => {
  const key = 'Futongdongfang!@'
  const tempKey = CryptoJS.enc.Utf8.parse(key)
  const srcs = CryptoJS.enc.Utf8.parse(value)
  const encrypted = CryptoJS.AES.encrypt(srcs, tempKey, {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7
  })
  return encrypted.toString()
}
