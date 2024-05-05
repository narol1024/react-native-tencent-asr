import { isObject, isArray, camelCase } from 'lodash-es';

export function transformToJson(result: string): object {
  if (typeof result === 'object') {
    return result;
  }
  try {
    return JSON.parse(result as unknown as string);
  } catch (error) {
    return {};
  }
}

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
