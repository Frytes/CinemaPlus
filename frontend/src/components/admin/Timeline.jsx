import { useState, useEffect, useRef } from 'react';

const Timeline = ({
    selectedDateStr,
    sessions,
    previewSession,
    isOverlap,
    onTimeChange,
    disabled,
    onDragAttemptWithoutMovie,
    onSessionClick
}) => {
    const containerRef = useRef(null);
    const [isDragging, setIsDragging] = useState(false);
    const [dragOffsetX, setDragOffsetX] = useState(0);
    const [hoveredSessionId, setHoveredSessionId] = useState(null);
    const [clickWithoutPreview, setClickWithoutPreview] = useState(false);

    const START_HOUR = 6;
    const TOTAL_HOURS = 22;

    const timelineStart = new Date(`${selectedDateStr}T06:00:00`);
    const totalDurationMs = TOTAL_HOURS * 60 * 60 * 1000;

    const getPercent = (date) => {
        const d = new Date(date);
        const diff = d.getTime() - timelineStart.getTime();
        if (diff < 0) return -1000;
        return (diff / totalDurationMs) * 100;
    };

    const snapToGrid = (date) => {
        const newDate = new Date(date);
        const minutes = newDate.getMinutes();
        const snappedMinutes = Math.round(minutes / 10) * 10;
        newDate.setMinutes(snappedMinutes);
        newDate.setSeconds(0);
        return newDate;
    };

    const calculateTimeFromMouseX = (clientX) => {
        if (!containerRef.current) return null;
        const rect = containerRef.current.getBoundingClientRect();
        const offsetX = clientX - rect.left;
        let percent = offsetX / rect.width;
        if (percent < 0) percent = 0;
        if (percent > 1) percent = 1;

        const timeMs = timelineStart.getTime() + (percent * totalDurationMs);
        const snappedTime = snapToGrid(new Date(timeMs));

        const hh = String(snappedTime.getHours()).padStart(2, '0');
        const mm = String(snappedTime.getMinutes()).padStart(2, '0');
        return `${hh}:${mm}`;
    };

    // Клик по полосе таймлайна (без ползунка)
    const handleTimelineClick = (e) => {
        if (disabled) {
            onDragAttemptWithoutMovie();
            return;
        }

        // Просто устанавливаем время в точке клика (ползунок подъедет)
        const newTime = calculateTimeFromMouseX(e.clientX);
        if (newTime) {
            onTimeChange(newTime);
        }
        setClickWithoutPreview(true);

        e.preventDefault();
    };

    // Перетаскивание ползунка
    const handleSliderMouseDown = (e) => {
        if (disabled) {
            onDragAttemptWithoutMovie();
            return;
        }

        if (!previewSession) return;

        // Вычисляем смещение клика внутри ползунка
        const containerRect = containerRef.current.getBoundingClientRect();
        const mouseX = e.clientX;

        const previewStartPercent = getPercent(previewSession.start);
        const previewStartPx = (previewStartPercent / 100) * containerRect.width;
        const mouseOffsetFromStart = mouseX - (containerRect.left + previewStartPx);

        setDragOffsetX(mouseOffsetFromStart);
        setIsDragging(true);
        setClickWithoutPreview(false);

        // Устанавливаем новое время с учетом смещения
        const newTime = calculateTimeFromMouseX(mouseX - mouseOffsetFromStart);
        if (newTime) {
            onTimeChange(newTime);
        }

        e.preventDefault();
        e.stopPropagation();
    };

    const handleMouseMove = (e) => {
        if (!isDragging) return;

        // Перемещаем ползунок с учетом смещения
        const newTime = calculateTimeFromMouseX(e.clientX - dragOffsetX);
        if (newTime) {
            onTimeChange(newTime);
        }
    };

    useEffect(() => {
        const handleGlobalMouseMove = (e) => handleMouseMove(e);
        const handleMouseUp = () => {
            setIsDragging(false);
            setDragOffsetX(0);
        };

        if (isDragging) {
            window.addEventListener('mousemove', handleGlobalMouseMove);
            window.addEventListener('mouseup', handleMouseUp);
            document.body.style.cursor = 'grabbing';
        }

        return () => {
            window.removeEventListener('mousemove', handleGlobalMouseMove);
            window.removeEventListener('mouseup', handleMouseUp);
            document.body.style.cursor = '';
        };
    }, [isDragging, dragOffsetX]);

    const getPreviewPosition = () => {
        if (!previewSession) return null;

        const left = getPercent(previewSession.start);
        let right = getPercent(previewSession.end);
        if (right < left) right = 100;
        const width = right - left;

        if (left > 100 || right < 0) return null;

        const safeLeft = Math.max(0, left);
        const safeWidth = Math.min(100 - safeLeft, width - (safeLeft - left));

        if (safeWidth <= 0) return null;

        return { left: safeLeft, width: safeWidth };
    };

    const previewPos = getPreviewPosition();

    return (
        <div style={{userSelect:'none', marginBottom: '20px'}}>
            <div style={{display:'flex', justifyContent:'space-between', fontSize:'0.75rem', color:'#666', marginBottom:'5px'}}>
                {[...Array(12)].map((_, i) => {
                    let h = START_HOUR + (i * 2);
                    if (h >= 24) h -= 24;
                    return <span key={i} style={{width:'30px', textAlign:'center'}}>{h.toString().padStart(2,'0')}:00</span>
                })}
            </div>

            <div
                ref={containerRef}
                onMouseDown={handleTimelineClick}
                style={{
                    height:'60px', background:'#1a1a1a', border:'1px solid #444', borderRadius:'8px',
                    position:'relative', overflow:'hidden',
                    cursor: disabled ? 'not-allowed' : 'pointer'
                }}
            >
                {[...Array(TOTAL_HOURS + 1)].map((_, i) => (
                    <div key={i} style={{
                        position:'absolute', left:`${(i/TOTAL_HOURS)*100}%`, top:0, bottom:0,
                        width:'1px', background:'rgba(255,255,255,0.08)'
                    }}></div>
                ))}

                {sessions.map(s => {
                    const left = getPercent(s.startTime);
                    let right = getPercent(s.endTime);
                    if (right < left) right = 100;
                    const width = right - left;

                    if (left > 100 || right < 0) return null;
                    const safeLeft = Math.max(0, left);
                    const safeWidth = Math.min(100 - safeLeft, width - (safeLeft - left));
                    if (safeWidth <= 0) return null;

                    const isHovered = hoveredSessionId === s.id;

                    return (
                        <div key={s.id}
                             title={`${s.movieTitle}\nНажмите для удаления`}
                             onMouseEnter={() => setHoveredSessionId(s.id)}
                             onMouseLeave={() => setHoveredSessionId(null)}
                             onClick={(e) => {
                                 e.stopPropagation();
                                 if (onSessionClick) onSessionClick(s);
                             }}
                             onMouseDown={(e) => e.stopPropagation()}
                             style={{
                                 position:'absolute', left:`${safeLeft}%`, width:`${safeWidth}%`, top:'10px', bottom:'10px',
                                 background: isHovered ? '#e74c3c' : '#34495e',
                                 borderRadius:'4px', border: isHovered ? '1px solid #c0392b' : '1px solid #5d6d7e',
                                 zIndex: 2,
                                 overflow:'hidden', fontSize:'0.7rem', color:'#ccc',
                                 display:'flex', alignItems:'center', justifyContent:'space-between',
                                 padding:'0 5px', pointerEvents:'auto', cursor:'pointer',
                                 transition: 'background 0.2s'
                             }}>
                            <span style={{whiteSpace:'nowrap', overflow:'hidden', textOverflow:'ellipsis'}}>{s.movieTitle}</span>
                            {isHovered && <span style={{fontWeight:'bold', color:'white'}}>🗑️</span>}
                        </div>
                    );
                })}

                {previewPos && (
                    <div
                        onMouseDown={handleSliderMouseDown}
                        style={{
                            position:'absolute',
                            left:`${previewPos.left}%`,
                            width:`${previewPos.width}%`,
                            top:'5px',
                            bottom:'5px',
                            background: isOverlap ? 'rgba(231, 76, 60, 0.9)' : 'rgba(46, 204, 113, 0.9)',
                            borderRadius:'6px',
                            border: isOverlap ? '2px solid #c0392b' : '2px solid #27ae60',
                            zIndex:10,
                            cursor: isDragging ? 'grabbing' : 'grab',
                            display:'flex',
                            alignItems:'center',
                            justifyContent:'center',
                            color:'white',
                            fontWeight:'bold',
                            fontSize:'0.8rem',
                            textShadow:'0 1px 2px black',
                            transition: clickWithoutPreview ? 'left 0.2s ease' : 'left 0s ease',
                            boxShadow: isDragging ? '0 0 15px rgba(255,255,255,0.2)' : 'none',
                            pointerEvents: 'auto'
                        }}
                    >
                        {isOverlap ? '⛔' : '✥'}
                    </div>
                )}
            </div>
        </div>
    );
};

export default Timeline;