import { isObject, isArray, camelCase } from 'lodash-es';

type Data = {
  [key: string]: number | string | Data;
};

// 安全转换为json字符串
export function normalizedJson(data: Data): Data {
  if (typeof data === 'string') {
    try {
      data = JSON.parse(data);
    } catch (e) {}
  }
  for (const key in data) {
    if (typeof data[key] === 'string') {
      try {
        data[key] = JSON.parse(data[key] as string);
        if (typeof data[key] === 'object' && data[key] !== null) {
          data[key] = normalizedJson(data[key] as Data);
        }
      } catch (e) {
        continue;
      }
    } else if (typeof data[key] === 'object' && data[key] !== null) {
      data[key] = normalizedJson(data[key] as Data);
    }
  }
  return data;
}

// 将key转换为小驼峰
export function keysToCamelCase<T>(obj: T): T {
  if (isArray(obj)) {
    return obj.map((item) => keysToCamelCase(item)) as unknown as T;
  } else if (isObject(obj)) {
    return Object.keys(obj).reduce((result: any, key) => {
      const camelKey = camelCase(key) as keyof T;
      result[camelKey] = keysToCamelCase((obj as any)[key]);
      return result;
    }, {} as T);
  }
  return obj;
}
