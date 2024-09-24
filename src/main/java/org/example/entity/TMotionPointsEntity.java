package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author 吴子木
 * @since 2024-09-23
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_motion_points")
public class TMotionPointsEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * UNIX Timestamp of the sample
     */
    private LocalDateTime recordTime;

    /**
     * Pose point X0 (relative to frame size, from Mediapipe)
     */
    private String x;

    /**
     * Pose point Y0 (relative to frame size, from Mediapipe)
     */
    private String y;

    /**
     * Pose point Z0 (relative to frame size, from Mediapipe)
     */
    private String z;

    /**
     * Pose point DZ0 (millimeters, from Depth Cam)
     */
    private String temperature;

    /**
     * Data id belong to t_motion_data_points
     */
    private String dataId;

    private Integer userId;


}
