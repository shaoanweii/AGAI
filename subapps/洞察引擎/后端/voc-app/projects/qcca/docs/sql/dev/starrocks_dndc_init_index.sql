-- SHOW index from mv_dws_voc2_computed_result_all_data
-- DROP INDEX idx_channel_cod on mv_dws_voc2_computed_result_all_data
-- SHOW TABLE STATUS


-- 低基数列 BITMAP
CREATE INDEX idx_channel_code		ON mv_dws_voc2_computed_result_all_data(	channel_code	) ;
CREATE INDEX idx_content_type		ON mv_dws_voc2_computed_result_all_data(	content_type	) ;
CREATE INDEX idx_sentiment			ON mv_dws_voc2_computed_result_all_data(	sentiment	) ;
CREATE INDEX idx_intention			ON mv_dws_voc2_computed_result_all_data(	intention	) ;
CREATE INDEX idx_is_outer			ON mv_dws_voc2_computed_result_all_data(	is_outer	) ;
CREATE INDEX idx_cust_gender		ON mv_dws_voc2_computed_result_all_data(	cust_gender	) ;
CREATE INDEX idx_cust_province_code	ON mv_dws_voc2_computed_result_all_data(	cust_province_code	) ;
CREATE INDEX idx_cust_type			ON mv_dws_voc2_computed_result_all_data(	cust_type	) ;
CREATE INDEX idx_dlr_big_area_code	ON mv_dws_voc2_computed_result_all_data(	dlr_big_area_code	) ;
CREATE INDEX idx_dlr_province_code	ON mv_dws_voc2_computed_result_all_data(	dlr_province_code	) ;
CREATE INDEX idx_dlr_status			ON mv_dws_voc2_computed_result_all_data(	dlr_status	) ;
CREATE INDEX idx_is_big_v			ON mv_dws_voc2_computed_result_all_data(	is_big_v	) ;
CREATE INDEX idx_vtr_tag_first_code	ON mv_dws_voc2_computed_result_all_data(	vtr_tag_first_code	) ;
CREATE INDEX idx_com_tag_first_code	ON mv_dws_voc2_computed_result_all_data(	com_tag_first_code	) ;
CREATE INDEX idx_adb_tag_first_code	ON mv_dws_voc2_computed_result_all_data(	adb_tag_first_code	) ;
CREATE INDEX idx_cj_tag_first_code	ON mv_dws_voc2_computed_result_all_data(	cj_tag_first_code	) ;
CREATE INDEX idx_nps_tag_first_code	ON mv_dws_voc2_computed_result_all_data(	nps_tag_first_code	) ;
-- bloom_filter_columns
-- ALTER MATERIALIZED VIEW mv_dws_voc2_computed_result_all_data SET ("bloom_filter_columns" = "data_create_time, car_series_code,sentiment,intention,dlr_province_code,dlr_city_code");

CREATE INDEX idx_tag_type	ON voc2_ins_tags_info_m_v(	tag_type	) ;


-- 低基数列 BITMAP
CREATE INDEX idx_channel_id	    ON dwd_voc2_pre_rules_result_data(	channel_id	) ;
CREATE INDEX idx_bandon		    ON dwd_voc2_pre_rules_result_data(	abandon	) ;
CREATE INDEX idx_done		    ON dwd_voc2_pre_rules_result_data(	done	) ;

-- 低基数列 BITMAP
CREATE INDEX idx_channel_id		ON dwd_voc2_all_meta_data(	channel_id	) ;
CREATE INDEX idx_done			ON dwd_voc2_all_meta_data(	done	) ;
CREATE INDEX idx_data_status	ON dwd_voc2_all_meta_data(	data_status	) ;

-- 低基数列 BITMAP
CREATE INDEX idx_channel_code	ON dwd_voc2_private_domain_all_data(	channel_code	) ;
CREATE INDEX idx_brand_code		ON dwd_voc2_private_domain_all_data(	brand_code	) ;
CREATE INDEX idx_content_type	ON dwd_voc2_private_domain_all_data(	content_type	) ;

-- 低基数列 BITMAP
CREATE INDEX idx_channel_code	ON dwd_voc2_public_domain_all_data(	channel_code	) ;
CREATE INDEX idx_brand_code		ON dwd_voc2_public_domain_all_data(	brand_code	) ;
CREATE INDEX idx_content_type	ON dwd_voc2_public_domain_all_data(	content_type	) ;

-- 低基数列 BITMAP
CREATE INDEX idx_channel_id	    ON dwd_voc2_post_rules_result_data(	channel_id	) ;
CREATE INDEX idx_content_type	ON dwd_voc2_post_rules_result_data(	content_type	) ;
CREATE INDEX idx_brand_code	    ON dwd_voc2_post_rules_result_data(	brand_code	) ;
CREATE INDEX idx_abandon	    ON dwd_voc2_post_rules_result_data(	abandon	) ;
CREATE INDEX idx_done	        ON dwd_voc2_post_rules_result_data(	done	) ;
-- bloom_filter_columns
ALTER TABLE dwd_voc2_post_rules_result_data SET ("bloom_filter_columns" = "topic,sentiment,intention_type,one_id");
