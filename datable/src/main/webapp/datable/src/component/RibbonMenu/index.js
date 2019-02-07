import React from "react";
import intl from 'react-intl-universal';

import {Tab, TabList, Tabs, TabPanel} from "../Tab";

import './index.scss';
import '../Tab/style/react-tabs.css';

class RibbonMenu extends React.Component {

    constructor(props) {
        super(props);

    }

    static defaultProps = {
        // 默认菜单项，各项功能参考excel
        topMenuList: ["File", "Home", "Insert", "Data", "Review", "View", "Extension", "Help",],
    };

    render() {

        const {
            topMenuList,
            setSheetReadOnly,
            readOnlyState,
        } = this.props;

        return (
            <div className="menuHeight">
                {/*<Tabs defaultIndex={1} className="menuHeight">*/}
                <Tabs defaultIndex={1}>
                    <TabList>
                        {
                            topMenuList.map(function (item, i) {

                                // 第一个tab样式特殊处理，discuss
                                if (item === "File") {
                                    // return <Tab key={i} className="">{`${intl.get(item)}`}</Tab>
                                    return <Tab key={i}>{`${intl.get(item)}`}</Tab>
                                }

                                return <Tab key={i}>{`${intl.get(item)}`}</Tab>
                            })
                        }
                    </TabList>

                    {
                        topMenuList.map(function (item, i) {

                                if (item === "File") {
                                    return (
                                        <TabPanel key={i}>
                                            <a className="button tabPanelIcon--default">
                                                <i className="fa fa-folder-open-o"/>
                                            </a>
                                            <a className="button tabPanelIcon--default">
                                                <i className="fa fa-save"/>
                                            </a>
                                            <a className="button tabPanelIcon--default">
                                                <i className="fa fa-file-o"/>
                                            </a>
                                        </TabPanel>);
                                }

                                if (item === "Home") {
                                    return (
                                        <TabPanel key={i}>
                                            <input id="readOnlyCheck"
                                                   type="checkbox"
                                                   onChange={setSheetReadOnly}
                                                   defaultChecked={readOnlyState}
                                            />
                                            <label htmlFor="readOnlyCheck">
                                                Toggle <code>readOnly</code>
                                            </label>

                                            <a className=" tabPanelIcon--default">
                                                {/*<FontAwesomeIcon icon={['fas', 'bold']}/>*/}
                                                <i className="fa fa-bold"/>
                                            </a>
                                            <a className=" tabPanelIcon--default">
                                                {/*<FontAwesomeIcon icon={['fas', 'italic']}/>*/}
                                                <i className="fa fa-italic"/>
                                            </a>
                                            <a className=" tabPanelIcon--default">
                                                {/*<FontAwesomeIcon icon={['fas', 'paint-brush']}/>*/}
                                                <i className="fa fa-paint-brush"/>
                                            </a>
                                        </TabPanel>);
                                }

                                // if (item === "Insert") {
                                //     return (
                                //         <TabPanel key={i}>
                                //             <a className="button tabPanelIcon--default">
                                //                 <FontAwesomeIcon icon={['far', 'image']}/>
                                //             </a>
                                //             <a className="button tabPanelIcon--default">
                                //                 <FontAwesomeIcon icon={['fas', 'table']}/>
                                //             </a>
                                //             <a className="button tabPanelIcon--default">
                                //                 <FontAwesomeIcon icon={['far', 'chart-bar']}/>
                                //             </a>
                                //         </TabPanel>);
                                // }
                                //
                                // if (item === "Data") {
                                //     return (
                                //         <TabPanel key={i}>
                                //             <a className="button tabPanelIcon--default">
                                //                 <FontAwesomeIcon icon={['fas', 'filter']}/>
                                //             </a>
                                //             <a className="button tabPanelIcon--default">
                                //                 <FontAwesomeIcon icon={['fas', 'sort-amount-up']}/>
                                //             </a>
                                //             <a className="button tabPanelIcon--default">
                                //                 <FontAwesomeIcon icon={['far', 'clone']}/>
                                //             </a>
                                //         </TabPanel>);
                                // }
                                //
                                // if (item === "Review") {
                                //     return (
                                //         <TabPanel key={i}>
                                //             <a className="button tabPanelIcon--default">
                                //                 <FontAwesomeIcon icon={['fas', 'language']}/>
                                //             </a>
                                //             <a className="button tabPanelIcon--default">
                                //                 <FontAwesomeIcon icon={['far', 'comment']}/>
                                //             </a>
                                //             <a className="button tabPanelIcon--default">
                                //                 <FontAwesomeIcon icon={['fas', 'angle-double-left']}/>
                                //             </a>
                                //             <a className="button tabPanelIcon--default">
                                //                 <FontAwesomeIcon icon={['fas', 'angle-double-right']}/>
                                //             </a>
                                //         </TabPanel>);
                                // }
                                //
                                // if (item === "View") {
                                //     return (
                                //         <TabPanel key={i}>
                                //             <a className="button tabPanelIcon--default">
                                //                 <FontAwesomeIcon icon={['fas', 'grip-lines']}/>
                                //             </a>
                                //             <a className="button tabPanelIcon--default">
                                //                 <FontAwesomeIcon icon={['fas', 'heading']}/>
                                //             </a>
                                //             <a className="button tabPanelIcon--default">
                                //                 <FontAwesomeIcon icon={['fas', 'palette']}/>
                                //             </a>
                                //         </TabPanel>);
                                // }
                                // if (item === "Extension") {
                                //     return (
                                //         <TabPanel key={i}>
                                //             <a className="button tabPanelIcon--default">
                                //                 <FontAwesomeIcon icon={['fas', 'tablets']}/>
                                //             </a>
                                //             <a className="button tabPanelIcon--default">
                                //                 <FontAwesomeIcon icon={['fas', 'chart-pie']}/>
                                //             </a>
                                //             <a className="button tabPanelIcon--default">
                                //                 <FontAwesomeIcon icon={['far', 'map']}/>
                                //             </a>
                                //         </TabPanel>);
                                // }
                                // if (item === "Help") {
                                //     return (
                                //         <TabPanel key={i}>
                                //             <a className="button tabPanelIcon--default">
                                //                 <FontAwesomeIcon icon={['far', 'keyboard']}/>
                                //             </a>
                                //             <a className="button tabPanelIcon--default">
                                //                 <FontAwesomeIcon icon={['fab', 'github']}/>
                                //             </a>
                                //             <a className="button tabPanelIcon--default">
                                //                 <FontAwesomeIcon icon={['far', 'comments']}/>
                                //             </a>
                                //         </TabPanel>);
                                // }

                                return (
                                    <TabPanel key={i}>
                                        <a className="button">
                                            <span className="icon is-small">
                                            <i className="fa fa-bold"/>
                                            </span>
                                            {/*<FontAwesomeIcon icon={['far', 'folder-open']}/>*/}

                                        </a>
                                        <a className="button">
                                            {/*<span className="icon is-small">*/}
                                            <i className="fa fa-italic"/>
                                            {/*</span>*/}
                                            {/*<FontAwesomeIcon icon={['far', 'save']}/>*/}
                                        </a>
                                    </TabPanel>);
                            }
                        )
                    }
                </Tabs>
            </div>
        )
            ;
    }
}

export default RibbonMenu;
