package org.easyframework.report.engine.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 纵向展示报表统计列的报表数据集类。
 * 
 *
 */
public class VerticalStatColumnDataSet extends ReportDataSet {
	/**
	 * 
	 * @param metaDataSet
	 * @param layout
	 * @param statColumnLayout
	 */
	public VerticalStatColumnDataSet(ReportMetaDataSet metaDataSet, LayoutType layout, LayoutType statColumnLayout) {
		super(metaDataSet, layout, statColumnLayout);
	}

	/**
	 * 获取报表数据集(RowMap)每一行的key
	 * 
	 * @param rowNode
	 *            行结点
	 * @param columnNode
	 *            列结点
	 * @return 行key
	 */
	@Override
	public String getRowKey(ColumnTreeNode rowNode, ColumnTreeNode columnNode) {
		return layout == LayoutType.HORIZONTAL ?
				(columnNode.getPath() + rowNode.getPath()) : (rowNode.getPath() + columnNode.getPath());
	}

	/**
	 * 获取报表表头(header)左边固定列集合
	 * 
	 * @return List<ReportDataColumn>
	 */
	@Override
	public List<ReportDataColumn> getHeaderLeftFixedColumns() {
		List<ReportDataColumn> columns = (layout == LayoutType.HORIZONTAL) ? this.getDimColumns() : this.getLayoutColumns();
		List<ReportDataColumn> leftFixedColumns = new ArrayList<ReportDataColumn>(columns.size() + 1);
		leftFixedColumns.addAll(columns);
		leftFixedColumns.add(new ReportDataColumn(new ReportMetaDataColumn("stat_column", "统计列", ColumnType.STATISTICAL)));
		return leftFixedColumns;
	}

	/**
	 * 获取表头(header)右边列树型结构
	 * 
	 * @return ColumnTree
	 */
	@Override
	public ColumnTree getHeaderRightColumnTree() {
		if (this.layout == LayoutType.HORIZONTAL) {
			return this.getLayoutColumnTree();
		}
		return this.getDimColumnTree();
	}

	/**
	 * 获取报表表体(body)左边固定列树型结构
	 * 
	 * @return ColumnTree
	 */
	@Override
	public ColumnTree getBodyLeftFixedColumnTree() {
		return layout == LayoutType.HORIZONTAL ?
				this.getHorizontalLayoutLeftFixedColumnTree() : this.getVerticalLayoutLeftFixedColumnTree();
	}

	/**
	 * 获取报表表体(body)右边列节点集合
	 * 
	 * @return List<ColumnTreeNode>
	 */
	@Override
	public List<ColumnTreeNode> getBodyRightColumnNodes() {
		return layout == LayoutType.HORIZONTAL ?
				this.getLayoutColumnTree().getLastLevelNodes() : this.getDimColumnTree().getLastLevelNodes();
	}

	/**
	 * 获取布局列树
	 * 
	 * @return ColumnTree
	 */
	@Override
	public ColumnTree getLayoutColumnTree() {
		if (this.layoutColumnTree != null) {
			return this.layoutColumnTree;
		}
		this.layoutColumnTree = this.buildColumnTreeByLevel(this.getLayoutColumns(), true);
		return this.layoutColumnTree;
	}

	/**
	 * 获取维度列树
	 * 
	 * @return ColumnTree
	 */
	@Override
	public ColumnTree getDimColumnTree() {
		if (this.dimColumnTree != null) {
			return this.dimColumnTree;
		}

		int depth = this.getDimColumnCount();
		// 无维度列则直接设置树只有一个节点
		if (depth == 0) {
			List<ColumnTreeNode> roots = new ArrayList<ColumnTreeNode>();
			roots.add(new ColumnTreeNode("stat_value", "值", ""));
			this.dimColumnTree = new ColumnTree(roots, 1);
			this.dimColumnTree.setLeafNodes(roots);
			return this.dimColumnTree;
		}

		this.dimColumnTree = this.buildColumnTreeByLevel(this.getDimColumns(), true);
		return this.dimColumnTree;
	}

	/**
	 * 获取报表统计列树
	 * 
	 * @return ColumnTree
	 */
	@Override
	public ColumnTree getStatColumnTree() {
		if (this.statColumnTree != null) {
			return this.statColumnTree;
		}

		List<ColumnTreeNode> treeNodes = new ArrayList<ColumnTreeNode>();
		// 当统计列只有一列时,如果维度列大于0
		// 则表头列不显示出统计列，只显示布局列或维度列
		if (this.getEnabledStatColumns().size() == 1) {
			if (this.layout == LayoutType.HORIZONTAL || this.getDimColumnCount() > 0) {
				this.statColumnTree = new ColumnTree(treeNodes, 0);
				return this.statColumnTree;
			}
		}

		int depth = 1;
		List<ReportDataStatColumn> roots = this.getReportDataStatColumns();
		for (ReportDataStatColumn root : roots) {
			if (root.getReportDataColumn().getMetaData().isHidden()) {
				continue;
			}
			ColumnTreeNode treeNode = this.createColumnTreeNode(root.getReportDataColumn());
			for (ReportDataColumn child : root.getChildren()) {
				depth = 2;
				ColumnTreeNode childNode = this.createColumnTreeNode(child);
				if (root.getChildren().size() == 1) {
					childNode.setValue("");
				}
				treeNode.getChildren().add(childNode);
			}
			treeNodes.add(treeNode);
		}

		this.statColumnTree = new ColumnTree(treeNodes, depth);
		return this.statColumnTree;
	}

	/**
	 * 获取报表所有维度列列表
	 * 
	 * @return {@link List<ReportDataColumn>}
	 */
	@Override
	public List<ReportDataColumn> getDimColumns() {
		if (this.dimColumns != null) {
			return this.dimColumns;
		}

		this.dimColumns = new ArrayList<ReportDataColumn>();
		List<ReportMetaDataColumn> metaDataColumns = this.metaDataSet.getDimColumns();
		for (ReportMetaDataColumn metaDataColumn : metaDataColumns) {
			this.dimColumns.add(this.createColumn(metaDataColumn));
		}
		return this.dimColumns;
	}

	private ColumnTree getHorizontalLayoutLeftFixedColumnTree() {
		// 无维度列则表体左边固定列直接设置为一列统计列
		if (this.getDimColumnCount() == 0) {
			List<ColumnTreeNode> roots = new ArrayList<ColumnTreeNode>();
			roots.add(new ColumnTreeNode("stat_column", "统计列", ""));
			this.statColumnTree = new ColumnTree(roots, 1);
			this.statColumnTree.setLeafNodes(roots);
			return this.statColumnTree;
		}
		return this.getDimColumnTree();
	}

	private ColumnTree getVerticalLayoutLeftFixedColumnTree() {
		return this.getLayoutColumnTree();
	}
}