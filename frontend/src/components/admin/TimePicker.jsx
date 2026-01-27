import React, { useState, useEffect, useRef } from 'react';

const TimePicker = ({
    value,
    onChange,
    disabled = false,
    interval = 15,
    minTime = "00:00",
    maxTime = "23:59"
}) => {
    const [time, setTime] = useState(value || '10:00');
    const [showPicker, setShowPicker] = useState(false);
    const [isManualInput, setIsManualInput] = useState(false);
    const pickerRef = useRef(null);
    const inputRef = useRef(null);

    const generateTimeSlots = () => {
        const slots = [];
        const [minHour, minMinute] = minTime.split(':').map(Number);
        const [maxHour, maxMinute] = maxTime.split(':').map(Number);

        let hour = minHour;
        let minute = minMinute;

        while (hour < maxHour || (hour === maxHour && minute <= maxMinute)) {
            const hh = String(hour).padStart(2, '0');
            const mm = String(minute).padStart(2, '0');
            slots.push(`${hh}:${mm}`);

            minute += interval;
            if (minute >= 60) {
                hour += Math.floor(minute / 60);
                minute = minute % 60;
            }
        }

        return slots;
    };

    const timeSlots = generateTimeSlots();

    const isValidFormat = (timeStr) => {
        const regex = /^([0-1][0-9]|2[0-3]):[0-5][0-9]$/;
        return regex.test(timeStr);
    };

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (pickerRef.current && !pickerRef.current.contains(event.target)) {
                if (isManualInput && inputRef.current) {
                    const newTime = inputRef.current.value;
                    if (isValidFormat(newTime)) {
                        setTime(newTime);
                        onChange(newTime);
                    }
                }
                setShowPicker(false);
                setIsManualInput(false);
            }
        };

        if (showPicker || isManualInput) {
            document.addEventListener('mousedown', handleClickOutside);
        }

        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [showPicker, isManualInput]);

    useEffect(() => {
        if (value !== time) {
            setTime(value);
        }
    }, [value]);

    const handleTimeSelect = (slot) => {
        setTime(slot);
        onChange(slot);
        setIsManualInput(false);
        setShowPicker(false);
    };

    const handleManualInput = () => {
        setIsManualInput(true);
        setShowPicker(false);
        setTimeout(() => {
            if (inputRef.current) {
                inputRef.current.focus();
                inputRef.current.select();
            }
        }, 10);
    };

    const handleInputChange = (e) => {
        const newValue = e.target.value;
        if (newValue.length === 2 && !newValue.includes(':')) {
            e.target.value = newValue + ':';
        }
        if (newValue.length > 5) {
            e.target.value = newValue.slice(0, 5);
        }
    };

    const handleInputBlur = (e) => {
        const newTime = e.target.value;
        if (isValidFormat(newTime)) {
            setTime(newTime);
            onChange(newTime);
        }
        setIsManualInput(false);
    };

    const handleInputKeyDown = (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            const newTime = e.target.value;
            if (isValidFormat(newTime)) {
                setTime(newTime);
                onChange(newTime);
                setIsManualInput(false);
            }
        } else if (e.key === 'Escape') {
            e.target.value = time;
            setIsManualInput(false);
        }
    };

    const formatTimeDisplay = (timeStr) => {
        const [hours, minutes] = timeStr.split(':');
        return `${hours}:${minutes}`;
    };

    const inputStyle = {
        width: '100%',
        padding: '12px',
        background: '#333',
        border: '1px solid #444',
        borderRadius: '6px',
        color: 'white',
        fontSize: '1rem',
        cursor: disabled ? 'not-allowed' : 'pointer',
        boxSizing: 'border-box',
        outline: 'none'
    };

    return (
        <div style={{ position: 'relative', width: '100%' }} ref={pickerRef}>
            <div style={{ position: 'relative' }}>
                {isManualInput ? (
                    <input
                        ref={inputRef}
                        type="text"
                        defaultValue={time}
                        style={{ ...inputStyle, cursor: 'text' }}
                        onChange={handleInputChange}
                        onBlur={handleInputBlur}
                        onKeyDown={handleInputKeyDown}
                        placeholder="чч:мм"
                        pattern="[0-2][0-9]:[0-5][0-9]"
                        maxLength={5}
                    />
                ) : (
                    <input
                        type="text"
                        value={formatTimeDisplay(time)}
                        readOnly
                        style={inputStyle}
                        onClick={() => {
                            if (!disabled) {
                                setShowPicker(!showPicker);
                            }
                        }}
                        disabled={disabled}
                        onDoubleClick={handleManualInput}
                    />
                )}
                {!isManualInput && (
                    <span style={{
                        position: 'absolute',
                        right: '12px',
                        top: '50%',
                        transform: 'translateY(-50%)',
                        color: '#aaa',
                        pointerEvents: 'none',
                        fontSize: '1.2rem'
                    }}>
                        ⌚
                    </span>
                )}
            </div>

            {showPicker && !disabled && (
                <div style={{
                    position: 'absolute',
                    top: '100%',
                    left: 0,
                    right: 0,
                    background: '#1a1a1a',
                    border: '1px solid #444',
                    borderRadius: '6px',
                    zIndex: 1000,
                    maxHeight: '200px',
                    overflowY: 'auto',
                    boxShadow: '0 8px 30px rgba(0, 0, 0, 0.4)',
                    marginTop: '4px'
                }}>
                    <div style={{
                        padding: '8px',
                        borderBottom: '1px solid #444'
                    }}>
                        <button
                            type="button"
                            style={{
                                width: '100%',
                                padding: '8px',
                                background: '#333',
                                border: '1px solid #555',
                                borderRadius: '4px',
                                color: '#ccc',
                                cursor: 'pointer',
                                fontSize: '0.85rem',
                                textAlign: 'center',
                                outline: 'none'
                            }}
                            onClick={handleManualInput}
                            onMouseOver={(e) => e.target.style.background = '#3a3a3a'}
                            onMouseOut={(e) => e.target.style.background = '#333'}
                        >
                            Ввести вручную
                        </button>
                    </div>

                    <div style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(4, 1fr)',
                        gap: '4px',
                        padding: '8px'
                    }}>
                        {timeSlots.map((slot) => (
                            <button
                                key={slot}
                                type="button"
                                style={{
                                    padding: '8px 4px',
                                    background: slot === time ? '#e50914' : '#252525',
                                    border: `1px solid ${slot === time ? '#e50914' : '#333'}`,
                                    borderRadius: '4px',
                                    color: slot === time ? 'white' : '#ccc',
                                    cursor: 'pointer',
                                    fontSize: '0.85rem',
                                    textAlign: 'center',
                                    outline: 'none'
                                }}
                                onClick={() => handleTimeSelect(slot)}
                                onMouseOver={(e) => {
                                    if (slot !== time) {
                                        e.target.style.background = '#333';
                                        e.target.style.borderColor = '#555';
                                    }
                                }}
                                onMouseOut={(e) => {
                                    if (slot !== time) {
                                        e.target.style.background = '#252525';
                                        e.target.style.borderColor = '#333';
                                    }
                                }}
                            >
                                {slot}
                            </button>
                        ))}
                    </div>
                </div>
            )}

            <style>
                {`
                div::-webkit-scrollbar {
                    width: 6px;
                }
                div::-webkit-scrollbar-track {
                    background: #222;
                    border-radius: 3px;
                }
                div::-webkit-scrollbar-thumb {
                    background: #555;
                    border-radius: 3px;
                }
                div::-webkit-scrollbar-thumb:hover {
                    background: #777;
                }
                `}
            </style>
        </div>
    );
};

export default TimePicker;