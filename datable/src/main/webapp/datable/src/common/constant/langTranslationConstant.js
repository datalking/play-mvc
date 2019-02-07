import en_US from '../locales/en_US.json';
import zh_CN from '../locales/zh_CN.json';

// 支持的语言
export const SUPPORT_LOCALES = [
    {
        name: '中文',
        value: 'zh-CN'
    },
    {
        name: 'English',
        value: 'en-US'
    }
];

// 自定义语言文件
export const locales = {
    'en-US': en_US,
    'zh-CN': zh_CN,
};