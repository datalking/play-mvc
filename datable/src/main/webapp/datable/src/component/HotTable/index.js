import React from 'react';
import Handsontable from 'handsontable';
import {SettingsMapper} from './settingsMapper';

/**
 * A Handsontable-ReactJS wrapper.
 */
export default class HotTable extends React.Component {

    constructor() {
        super();

        this.hotInstance = null;
        this.settingsMapper = new SettingsMapper();
        this.id = null;
    }

    /**
     * Initialize Handsontable after the component has mounted.
     */
    componentDidMount() {
        const newSettings = this.settingsMapper.getSettings(this.props);
        this.hotInstance = new Handsontable(document.getElementById(this.id), newSettings);
    }

    /**
     * Call the `updateHot` method and prevent the component from re-rendering the instance.
     */
    shouldComponentUpdate(nextProps, nextState) {
        this.updateHot(this.settingsMapper.getSettings(nextProps));

        return false;
    }

    /**
     * Destroy the Handsontable instance when the parent component unmounts.
     */
    componentWillUnmount() {
        this.hotInstance.destroy();
    }

    /**
     * Render the table.
     *
     * @returns {XML}
     */
    render() {
        this.id = this.props.id || 'hot' + new Date().getTime();
        this.className = this.props.className || '';
        this.style = this.props.style || {};

        return <div id={this.id} className={this.className} style={this.style}></div>
    }

    /**
     * Call the `updateSettings` method for the Handsontable instance.
     *
     * @param newSettings
     */
    updateHot(newSettings) {
        this.hotInstance.updateSettings(newSettings);
    }
}
