const presets = [
    [
        "@babel/preset-env",
        {
            targets: {
                edge: "17",
                firefox: "50",
                chrome: "60",
                safari: "11.1",
            },
            useBuiltIns: "usage",
        },
    ],
    '@babel/preset-react',
];

const plugins = [
    '@babel/plugin-proposal-class-properties',
];

module.exports = {
    presets,
    plugins,
};