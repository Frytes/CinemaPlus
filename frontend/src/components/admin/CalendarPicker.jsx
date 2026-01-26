import React, { useState, useEffect, useRef } from 'react';

const CalendarPicker = ({ 
    value, 
    onChange, 
    disabled = false, 
    label = "",
    minDate = null,
    maxDate = null,
    highlightToday = true
}) => {
    const [date, setDate] = useState(value || new Date().toISOString().split('T')[0]);
    const [showCalendar, setShowCalendar] = useState(false);
    const [currentMonth, setCurrentMonth] = useState(new Date(date));
    const calendarRef = useRef(null);

    const today = new Date();
    const todayStr = today.toISOString().split('T')[0];

    useEffect(() => {
        if (value !== date) {
            setDate(value);
            setCurrentMonth(new Date(value));
        }
    }, [value]);

    // Закрытие календаря при клике вне компонента
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (calendarRef.current && !calendarRef.current.contains(event.target)) {
                setShowCalendar(false);
            }
        };

        if (showCalendar) {
            document.addEventListener('mousedown', handleClickOutside);
        }
        
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [showCalendar]);

    const handleDateSelect = (selectedDate) => {
        const dateStr = selectedDate.toISOString().split('T')[0];
        setDate(dateStr);
        onChange(dateStr);
        setShowCalendar(false);
    };

    const prevMonth = () => {
        setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1, 1));
    };

    const nextMonth = () => {
        setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1, 1));
    };

    const goToToday = () => {
        const today = new Date();
        setCurrentMonth(new Date(today.getFullYear(), today.getMonth(), 1));
        handleDateSelect(today);
    };

    const isDateDisabled = (day) => {
        if (!day) return true;
        
        const dateStr = day.toISOString().split('T')[0];
        
        if (minDate && dateStr < minDate) return true;
        if (maxDate && dateStr > maxDate) return true;
        
        return false;
    };

    const generateCalendar = () => {
        const year = currentMonth.getFullYear();
        const month = currentMonth.getMonth();
        
        const firstDay = new Date(year, month, 1);
        const lastDay = new Date(year, month + 1, 0);
        const daysInMonth = lastDay.getDate();
        
        const startDay = firstDay.getDay() === 0 ? 6 : firstDay.getDay() - 1; // Пн=0, Вс=6
        const days = [];

        // Пустые ячейки в начале
        for (let i = 0; i < startDay; i++) {
            days.push(null);
        }

        // Дни месяца
        for (let day = 1; day <= daysInMonth; day++) {
            days.push(new Date(year, month, day));
        }

        return days;
    };

    const dayNames = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];
    const monthNames = [
        'Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь',
        'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь'
    ];

    const formatDateDisplay = (dateStr) => {
        const [year, month, day] = dateStr.split('-');
        return `${day}.${month}.${year}`;
    };

    // Стили для инпута (такие же как у обычного input)
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

    const calendarDays = generateCalendar();

    return (
        <div style={{ position: 'relative', width: '100%' }} ref={calendarRef}>
            <div style={{ position: 'relative' }}>
                <input
                    type="text"
                    value={formatDateDisplay(date)}
                    readOnly
                    style={inputStyle}
                    onClick={() => !disabled && setShowCalendar(!showCalendar)}
                    disabled={disabled}
                />
                <span style={{
                    position: 'absolute',
                    right: '12px',
                    top: '50%',
                    transform: 'translateY(-50%)',
                    color: '#aaa',
                    pointerEvents: 'none',
                    fontSize: '1.2rem'
                }}>
                    📅
                </span>
            </div>
            
            {showCalendar && !disabled && (
                <div style={{
                    position: 'absolute',
                    top: 'calc(100% + 4px)',
                    left: 0,
                    width: '300px',
                    background: '#1a1a1a',
                    border: '1px solid #444',
                    borderRadius: '8px',
                    zIndex: 1000,
                    boxShadow: '0 8px 30px rgba(0, 0, 0, 0.4)',
                    overflow: 'hidden'
                }}>
                    <div style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        padding: '12px',
                        background: '#252525',
                        borderBottom: '1px solid #333'
                    }}>
                        <button 
                            type="button" 
                            style={{
                                background: 'transparent',
                                border: 'none',
                                color: '#aaa',
                                fontSize: '1.2rem',
                                cursor: 'pointer',
                                padding: '0 8px',
                                transition: 'color 0.2s'
                            }}
                            onClick={prevMonth}
                            onMouseOver={(e) => e.target.style.color = '#fff'}
                            onMouseOut={(e) => e.target.style.color = '#aaa'}
                        >
                            ‹
                        </button>
                        <span style={{
                            color: 'white',
                            fontWeight: '600',
                            fontSize: '0.95rem'
                        }}>
                            {monthNames[currentMonth.getMonth()]} {currentMonth.getFullYear()}
                        </span>
                        <button 
                            type="button" 
                            style={{
                                background: 'transparent',
                                border: 'none',
                                color: '#aaa',
                                fontSize: '1.2rem',
                                cursor: 'pointer',
                                padding: '0 8px',
                                transition: 'color 0.2s'
                            }}
                            onClick={nextMonth}
                            onMouseOver={(e) => e.target.style.color = '#fff'}
                            onMouseOut={(e) => e.target.style.color = '#aaa'}
                        >
                            ›
                        </button>
                    </div>
                    
                    <div style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(7, 1fr)',
                        padding: '8px 12px 4px',
                        background: '#1a1a1a'
                    }}>
                        {dayNames.map(day => (
                            <div key={day} style={{
                                textAlign: 'center',
                                color: '#888',
                                fontSize: '0.8rem',
                                fontWeight: '500',
                                padding: '4px 0'
                            }}>
                                {day}
                            </div>
                        ))}
                    </div>
                    
                    <div style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(7, 1fr)',
                        gap: '3px',
                        padding: '4px 12px 12px'
                    }}>
                        {calendarDays.map((day, index) => {
                            if (!day) {
                                return <div key={`empty-${index}`} style={{padding: '8px 0'}}></div>;
                            }
                            
                            const dateStr = day.toISOString().split('T')[0];
                            const isSelected = dateStr === date;
                            const isToday = highlightToday && dateStr === todayStr;
                            const isWeekend = day.getDay() === 0 || day.getDay() === 6;
                            const isDisabled = isDateDisabled(day);
                            
                            return (
                                <button
                                    key={dateStr}
                                    type="button"
                                    style={{
                                        background: isSelected ? '#e50914' : (isToday ? '#333' : '#252525'),
                                        border: `1px solid ${isSelected ? '#e50914' : (isToday ? '#e50914' : '#333')}`,
                                        borderRadius: '4px',
                                        color: isSelected ? 'white' : (isToday ? '#e50914' : (isWeekend ? '#ff6b6b' : '#ccc')),
                                        cursor: isDisabled ? 'not-allowed' : 'pointer',
                                        fontSize: '0.85rem',
                                        padding: '8px 0',
                                        textAlign: 'center',
                                        outline: 'none',
                                        opacity: isDisabled ? 0.5 : 1
                                    }}
                                    onClick={() => !isDisabled && handleDateSelect(day)}
                                    disabled={isDisabled}
                                    onMouseOver={(e) => {
                                        if (!isDisabled && !isSelected) {
                                            e.target.style.background = '#333';
                                            e.target.style.borderColor = '#555';
                                        }
                                    }}
                                    onMouseOut={(e) => {
                                        if (!isDisabled && !isSelected) {
                                            e.target.style.background = isToday ? '#333' : '#252525';
                                            e.target.style.borderColor = isToday ? '#e50914' : '#333';
                                        }
                                    }}
                                >
                                    {day.getDate()}
                                </button>
                            );
                        })}
                    </div>
                    
                    <div style={{
                        padding: '8px 12px',
                        background: '#252525',
                        borderTop: '1px solid #333',
                        textAlign: 'center'
                    }}>
                        <button 
                            type="button" 
                            style={{
                                background: '#333',
                                border: '1px solid #444',
                                borderRadius: '4px',
                                color: '#ccc',
                                cursor: 'pointer',
                                fontSize: '0.8rem',
                                padding: '6px 12px',
                                transition: 'all 0.2s'
                            }}
                            onClick={goToToday}
                            onMouseOver={(e) => {
                                e.target.style.background = '#444';
                                e.target.style.color = '#fff';
                            }}
                            onMouseOut={(e) => {
                                e.target.style.background = '#333';
                                e.target.style.color = '#ccc';
                            }}
                        >
                            Сегодня
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CalendarPicker;