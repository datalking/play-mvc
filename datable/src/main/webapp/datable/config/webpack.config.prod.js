const path = require('path');
const webpack = require('webpack');
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
// const FileManagerPlugin = require('filemanager-webpack-plugin');

module.exports = {
    entry: [
        './src/index.js',
    ],

    output: {
        path: path.join(__dirname, '../public'),
        // path: __dirname,
        filename: 'bundle.js',
    },

    module:
        {
            rules: [
                {
                    test: /\.(js|jsx)$/,
                    exclude: /node_modules/,
                    use: {
                        loader: 'babel-loader?babelrc=false&extends=' + path.resolve(__dirname, '../.babelrc')
                    },
                },
                {
                    test: /\.s?[ac]ss$/,
                    use: [
                        {loader: MiniCssExtractPlugin.loader, options: {}},
                        {loader: 'css-loader', options: {sourceMap: true}},
                        {loader: 'sass-loader', options: {sourceMap: true}}
                    ],
                },
                {
                    test: /.(ttf|otf|eot|svg|woff(2)?)(\?[a-z0-9]+)?$/,
                    use: [{
                        loader: 'file-loader',
                        options: {
                            name: '[name].[ext]',
                            outputPath: 'fonts/',    // where the fonts will go
                            // publicPath: 'fonts/'       // override the default path
                        }
                    }]
                },
                {
                    test: /\.(png|jpg|jpeg|gif)$/,
                    use: {
                        loader: 'url-loader',
                        options: {
                            limit: 250000 // Max file size = 250kb
                        }
                    }
                },
            ],
        },

    plugins:
        [
            new MiniCssExtractPlugin({
                path: path.join(__dirname, '../public'),
                filename: "bundle.css",
            }),
            new webpack.LoaderOptionsPlugin({
                debug: true
            }),
            new webpack.NamedModulesPlugin(),
            new webpack.HotModuleReplacementPlugin(),
            // new webpack.NoErrorsPlugin(),
            // new FileManagerPlugin({
            //     onEnd: [
            //         {
            //             copy: [
            //                 //目录是相对于执行npm run命令的根目录，而不是webpack.config.js的位置
            //                 {source: "./public", destination: "./docs"}
            //             ]
            //         },
            //     ]
            // }),
        ],

    // resolve: {
    //
    //     // 可以替换初始模块路径，此替换路径通过使用 resolve.alias 配置选项来创建一个别名
    //     alias: {
    //         'react': path.join(__dirname, '..', 'src/index.js'),
    //     },
    // },

    resolveLoader: {
        modules: [
            'node_modules',
        ],
    },

    mode: 'development',
    devtool: 'inline-source-map',
    devServer:
        {
            contentBase: path.join(__dirname, '../public'),
            port: 8900,
            publicPath: '/',
            // 设置自动刷新的方式，inline会在entry添加新入口，会在控制台显示reload状态，iframe会在页面header显示reload状态
            inline: true,
            // 启动热更新
            hot: true,
            // 在页面上全屏输出报错信息
            overlay: {
                warnings: true,
                errors: true
            },
            watchContentBase: true,
        }

};
