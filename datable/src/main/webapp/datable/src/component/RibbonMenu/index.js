import React from "react";
import intl from 'react-intl-universal';

import {Tab, TabList, Tabs, TabPanel} from "../Tab";
import '../../common/style/react-tabs.css';

class RibbonMenu extends React.Component {

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
            <Tabs defaultIndex={1}>
                <TabList>
                    {
                        topMenuList.map(function (item, i) {
                            return <Tab key={i}>{`${intl.get(item)}`}</Tab>
                        })
                    }
                </TabList>

                {
                    topMenuList.map(function (item, i) {

                            if (i === 1) {
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
                                    </TabPanel>);
                            }

                            return (
                                <TabPanel key={i}>
                                    <a className="button">
                                    <span className="icon is-small">
                                        <i className="fa fa-bold"></i>
                                    </span>
                                    </a>
                                    <a className="button">
                                    <span className="icon is-small">
                                        <i className="fa fa-italic"></i>
                                    </span>
                                    </a>
                                </TabPanel>);
                        }
                    )
                }
            </Tabs>
        );
    }
}

export default RibbonMenu;
